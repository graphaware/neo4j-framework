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

/**
 * An immutable pair of objects, which must be of the same type.
 *
 * @param <T> type of the objects in the pair.
 */
public class SameTypePair<T> extends Pair<T, T> {

    /**
     * Construct a new pair.
     *
     * @param first  first element, can be null.
     * @param second second element, can be null.
     */
    public SameTypePair(T first, T second) {
        super(first, second);
    }
}
