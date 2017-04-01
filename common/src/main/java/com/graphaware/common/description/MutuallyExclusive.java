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
 * Component that can determine, whether it is mutually exclusive with another instance.
 * <p/>
 * In the context of this project, mutually exclusive instances are instances that are {@link PartiallyComparable}
 * and do not share a common more specific instance in the lattice formed by the partial order.
 *
 * @param <T> type of object this can be mutually exclusive with.
 */
public interface MutuallyExclusive<T> {

    /**
     * Is this instance mutually exclusive with the given other instance? This method is reflexive.
     *
     * @param other to check mutual exclusivity against.
     * @return true iff this and the other are mutually exclusive.
     */
    boolean isMutuallyExclusive(T other);
}
