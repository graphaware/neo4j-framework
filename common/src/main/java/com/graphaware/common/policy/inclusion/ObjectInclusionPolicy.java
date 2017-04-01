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

package com.graphaware.common.policy.inclusion;

/**
 * {@link InclusionPolicy} deciding whether to include an object or not.
 *
 * @param <T> type of the object to make a decision about.
 */
public interface ObjectInclusionPolicy<T> extends InclusionPolicy {

    /**
     * Include the given object?
     *
     * @param object to check.
     * @return true iff the given object should be included.
     */
    boolean include(T object);
}
