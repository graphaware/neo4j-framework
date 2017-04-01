/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.util;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe sorted list of items and related quantities, which is bounded by a configurable maximum capacity.
 * Intended for frequent writes and occasional reads.
 * <p/>
 * The list is at all times sorted by the provided quantity comparator (natural ordering by default). Adding an element
 * that would be added after the last element of the list has no effect if the list if already full. Adding an element
 * to any position in the list means that the last element will be discarded. Adding an element that already exists in
 * the list is equivalent to removing the element and adding it again.
 * <p/>
 * Note that the list can be configured with a capacity and maxCapacity. An example will be used to illustrate the difference.
 * Let's say we want to keep track of 3 most popular items, where popularity is an integer number of likes. In that case
 * we would construct a list of capacity 3. However, if that really was the capacity of the list, then after adding items
 * with 100, 90, and 80 likes respectively would mean that an item with 79 likes will be ignored. Then, if the last item
 * in the list is "unliked" by 10 people, we end up with a list of 100, 90, and 70. This is not the truth, since there is
 * an item with 79 likes, which we've ignored. To account for these scenarios, it is possible to provide a maxCapacity, which
 * adds some extra space to the list. In the example above, a capacity of 3 and maxCapacity of 4 would prevent the anomaly,
 * since we would remember (but not show) the item with 79 likes.
 *
 * @param <T> type of the stored items.
 * @param <C> type of the quantities associated with the items.
 */
//todo allow finding values of items
public class BoundedSortedList<T, C extends Comparable<C>> {

    private final List<ComparableItem<T, C>> items = new ArrayList<>();
    private final Comparator<ComparableItem<T, C>> comparator;
    private final int capacity;
    private final int maxCapacity;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Construct a new list. Items are ordered using the natural ordering of associated quantities.
     *
     * @param capacity capacity (and maxCapacity) of the list.
     */
    public BoundedSortedList(int capacity) {
        this(capacity, capacity);
    }

    /**
     * Construct a new list.
     *
     * @param capacity   capacity (and maxCapacity) of the list.
     * @param comparator comparator for the quantities.
     */
    public BoundedSortedList(int capacity, final Comparator<C> comparator) {
        this(capacity, capacity, comparator);
    }

    /**
     * Construct a new list. Items are ordered using the natural ordering of associated quantities.
     *
     * @param capacity    capacity of the list (as perceived by users).
     * @param maxCapacity real capacity of the underlying list to mitigate the anomaly described in Javadoc of the class.
     */
    public BoundedSortedList(int capacity, int maxCapacity) {
        this(capacity, maxCapacity, new Comparator<C>() {
            @Override
            public int compare(C o1, C o2) {
                return o1.compareTo(o2);
            }
        });
    }

    /**
     * Construct a new list.
     *
     * @param capacity    capacity of the list (as perceived by users).
     * @param maxCapacity real capacity of the underlying list to mitigate the anomaly described in Javadoc of the class.
     * @param comparator  comparator for the quantities.
     */
    public BoundedSortedList(int capacity, int maxCapacity, final Comparator<C> comparator) {
        this.comparator = new Comparator<ComparableItem<T, C>>() {
            @Override
            public int compare(ComparableItem<T, C> o1, ComparableItem<T, C> o2) {
                return comparator.compare(o1.getQuantity(), o2.getQuantity());
            }
        };
        this.capacity = capacity;
        this.maxCapacity = maxCapacity;
    }

    /**
     * Get a items of this list.
     *
     * @return a list with the sorted items of the underlying list. The maximum number of items in the returned
     *         list is the "capacity" parameter passed into the constructor of this class.
     */
    public List<T> getItems() {
        List<T> result = new LinkedList<>();
        lock.readLock().lock();
        try {
            for (ComparableItem<T, C> comparableItem : items) {
                result.add(comparableItem.getItem());
                if (result.size() >= capacity) {
                    break;
                }
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Add an item and its associated quantity to the list.
     *
     * @param item     to add.
     * @param quantity of the item.
     */
    public void add(T item, C quantity) {
        ComparableItem<T, C> comparableItem = new ComparableItem<>(item, quantity);

        lock.readLock().lock();
        try {
            if (!items.contains(comparableItem) && items.size() >= maxCapacity && comparator.compare(items.get(items.size() - 1), comparableItem) < 0) {
                return;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            if (items.contains(comparableItem)) {
                items.remove(comparableItem);
            }

            items.add(comparableItem);

            Collections.sort(items, comparator);

            while (items.size() > maxCapacity) {
                items.remove(items.size() - 1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
