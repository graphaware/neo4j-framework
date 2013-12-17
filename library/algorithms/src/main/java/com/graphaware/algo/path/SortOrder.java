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

package com.graphaware.algo.path;

/**
 * How to sort the paths?
 */
public enum SortOrder {

    /**
     * By increasing length. Ordering of paths with the same lengths is unspecified.
     */
    LENGTH_ASC,

    /**
     * By increasing length, then by increasing cost. The cost property must be specified.
     */
    LENGTH_ASC_THEN_COST_ASC,

    /**
     * By increasing length, then by decreasing cost. The cost property must be specified.
     */
    LENGTH_ASC_THEN_COST_DESC
}