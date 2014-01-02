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

package com.graphaware.api.library.algo.path;

import java.util.Comparator;

/**
 * A comparator for {@link com.graphaware.api.library.algo.path.WeightedPath}s, taking into account their lengths at first,
 * then their costs (in case the lengths are the same).
 */
public class LengthThenCostWeightedPathComparator implements Comparator<WeightedPath> {

    private final SortOrder sortOrder;

    public enum SortOrder {
        ASC,
        DESC
    }

    /**
     * Construct a new comparator.
     *
     * @param sortOrder asc/desc.
     */
    public LengthThenCostWeightedPathComparator(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * First compare the length of the path, only then compare the total cost.
     */
    @Override
    public int compare(WeightedPath path1, WeightedPath path2) {
        int result = new Integer(path1.length()).compareTo(path2.length());

        if (result != 0) {
            return result;
        }

        switch (sortOrder) {
            case ASC:
                return new Long(path1.getCost()).compareTo(path2.getCost());
            case DESC:
                return new Long(path2.getCost()).compareTo(path1.getCost());
            default:
                throw new IllegalStateException("Unknown sort order " + sortOrder + ". This is a bug");
        }
    }
}
