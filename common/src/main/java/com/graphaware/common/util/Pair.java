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

import java.util.Objects;

/**
 * An immutable pair of objects.
 *
 * @param <FIRST>  type of the first object in the pair.
 * @param <SECOND> type of the second object in the pair.
 */
public class Pair<FIRST, SECOND> {

    private final FIRST first;
    private final SECOND second;

    /**
     * Construct a new pair.
     *
     * @param first first element, can be null.
     * @param second second element, can be null.
     */
    public Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Return the first element of the pair.
     *
     * @return first element, can be null.
     */
    public FIRST first() {
        return first;
    }

    /**
     * Return the second element of the pair.
     *
     * @return second element, can be null.
     */
    public SECOND second() {
        return second;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        if (second != null ? !second.equals(pair.second) : pair.second != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + Objects.toString(first) + ", " + Objects.toString(second) + ")";
    }

}
