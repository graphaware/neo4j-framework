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

import static org.springframework.util.Assert.notNull;

/**
 * A comparable item.
 * <p/>
 * Note that equality and hash code depend purely in the item, not on the quantity associated with it.
 *
 * @param <T> type of the item.
 * @param <C> type of the quantity associated with the item.
 */
public class ComparableItem<T, C extends Comparable<C>> implements Comparable<ComparableItem<T, C>> {

    private final T item;
    private final C quantity;

    /**
     * Construct a new comparable item.
     *
     * @param item     item, must not be null.
     * @param quantity quantity, must not be null.
     */
    public ComparableItem(T item, C quantity) {
        notNull(item);
        notNull(quantity);
        this.item = item;
        this.quantity = quantity;
    }

    public T getItem() {
        return item;
    }

    public C getQuantity() {
        return quantity;
    }

    @Override
    public int compareTo(ComparableItem<T, C> o) {
        return getQuantity().compareTo(o.getQuantity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComparableItem that = (ComparableItem) o;

        if (!item.equals(that.item)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return item.hashCode();
    }
}