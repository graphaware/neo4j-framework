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

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A stack (last-in-first-out) with a configurable maximum capacity. Optimised for high concurrent read throughput.
 * <p/>
 * When the number of elements in the stack exceeds its capacity, the oldest entries are dropped. Please note that
 * in multi-threaded environments, the size of the stack can temporarily exceed this number. This is a design decision
 * (to avoid synchronising writes).
 */
public class BoundedConcurrentStack<E> implements Iterable<E> {

    private final Deque<E> elements;
    private final int maxCapacity;

    /**
     * Construct a new stack.
     *
     * @param maxCapacity maximum capacity.
     */
    public BoundedConcurrentStack(int maxCapacity) {
        elements = new ConcurrentLinkedDeque<>();
        this.maxCapacity = maxCapacity;
    }

    /**
     * Push an element onto the stack.
     *
     * @param e to push.
     */
    public void push(E e) {
        while (elements.size() >= maxCapacity) {
            elements.removeLast();
        }
        elements.addFirst(e);
    }

    /**
     * Populate the stack. Please note that the elements are added to the stack in the reverse order than presented by the
     * input parameters. In other words, the first element of the input parameter will be returned first by this stack's
     * iterator.
     *
     * @param elements to populate the stack with.
     */
    public void populate(Collection<E> elements) {
        this.elements.addAll(elements);

        while (this.elements.size() > maxCapacity) {
            this.elements.removeLast();
        }
    }

    /**
     * Get iterator over the elements of this stack. Note that the order of iteration is the reverse of the order in which
     * elements were added.
     *
     * @return iterator.
     */
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    /**
     * @return true iff the stack is empty.
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }
}
