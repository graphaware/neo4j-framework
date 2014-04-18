/*
 * Copyright (c) 2014 GraphAware
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

package com.graphaware.library.algo.path;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

/**
 * Default production implementation of {@link com.graphaware.library.algo.path.PathCostCalculator}.
 */
public class PathCostCalculatorImpl implements PathCostCalculator {

    private final RelationshipCostFinder costFinder;

    /**
     * Construct new calculator.
     *
     * @param costFinder relationship cost finder.
     */
    public PathCostCalculatorImpl(RelationshipCostFinder costFinder) {
        this.costFinder = costFinder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long calculateCost(Path path) {
        long result = 0;

        for (Relationship r : path.relationships()) {
            long cost = costFinder.getCost(r);

            if (Long.MAX_VALUE == cost) {
                return Long.MAX_VALUE;
            }

            result += cost;
        }

        return result;
    }
}
