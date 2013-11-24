/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.change;

/**
 * Change in the state of an object, encapsulating the old (previous) and the new (current).
 */
public class Change<T> {

    private final T previous;
    private final T current;

    /**
     * Construct a change representation.
     *
     * @param previous state of the object.
     * @param current  state of the object.
     */
    public Change(T previous, T current) {
        this.previous = previous;
        this.current = current;
    }

    /**
     * Get the previous (old) state of the object.
     *
     * @return previous state
     */
    public T getPrevious() {
        return previous;
    }

    /**
     * Get the current (new) state of the object.
     *
     * @return current state
     */
    public T getCurrent() {
        return current;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change change = (Change) o;

        if (!current.equals(change.current)) return false;
        if (!previous.equals(change.previous)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = previous.hashCode();
        result = 31 * result + current.hashCode();
        return result;
    }
}
