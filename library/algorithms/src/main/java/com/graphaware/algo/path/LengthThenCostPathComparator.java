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

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.Comparator;

/**
 * A comparator for paths, taking into account their lengths at first, then their costs (in case the lengths are the same).
 */
public class LengthThenCostPathComparator implements Comparator<Path> {

    private final String costPropertyKey;
    private final SortOrder sortOrder;

    public enum SortOrder {
        ASC,
        DESC
    }

    /**
     * Construct a new comparator.
     *
     * @param costPropertyKey the key of the (integer) property that defines relationship cost in the path.
     * @param sortOrder       asc/desc.
     */
    public LengthThenCostPathComparator(String costPropertyKey, SortOrder sortOrder) {
        this.costPropertyKey = costPropertyKey;
        this.sortOrder = sortOrder;
    }

    /**
     * First compare the length of the path, only then compare the total cost.
     */
    @Override
    public int compare(Path path1, Path path2) {
        int result = new Integer(path1.length()).compareTo(path2.length());

        if (result != 0) {
            return result;
        }

        switch (sortOrder) {
            case ASC:
                return cost(path1).compareTo(cost(path2));
            case DESC:
                return cost(path2).compareTo(cost(path1));
            default:
                throw new IllegalStateException("Unknown sort order " + sortOrder + ". This is a bug");
        }
    }

    /**
     * Compute the cost of the path as the sum of relationship costs.
     *
     * @param path to compute cost for.
     * @return total cost.
     */
    private Integer cost(Path path) {
        int result = 0;
        for (Relationship r : path.relationships()) {
            if (!r.hasProperty(costPropertyKey)) {
                switch (sortOrder) {
                    case ASC:
                        return Integer.MAX_VALUE;
                    case DESC:
                        //add nothing to the cost
                        continue;
                    default:
                        throw new IllegalStateException("Unknown sort order " + sortOrder + ". This is a bug");
                }
            }
            result += (Integer) r.getProperty(costPropertyKey);
        }
        return result;
    }
}
