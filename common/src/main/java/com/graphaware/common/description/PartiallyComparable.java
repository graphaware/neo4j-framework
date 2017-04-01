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

package com.graphaware.common.description;

/**
 * Interface for types that can compare themselves to other types using a general to specific ordering.
 * <p/>
 * Since general-to-specific ordering is a partial (as opposed to total) ordering, note that
 * {@link #isMoreGeneralThan} and {@link #isMoreSpecificThan} can both return {@code false} for the same objects.
 * Likewise, if the objects are of equal generality, both methods can return {@code true}.
 *
 * @param <T> type that this can be compared to.
 */
public interface PartiallyComparable<T> {

    /**
     * Is this instance more general than (or at least as general as) the given instance?
     *
     * @param other to compare.
     * @return true iff this instance is more general than or as general as the provided instance.
     */
    boolean isMoreGeneralThan(T other);

    /**
     * Is this instance more specific than (or at least as specific as) the given instance?
     *
     * @param other to compare.
     * @return true iff this instance is more specific than or as specific as the provided instance.
     */
    boolean isMoreSpecificThan(T other);
}
