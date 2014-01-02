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

package com.graphaware.algo.path;

import org.neo4j.graphdb.Relationship;

/**
 * {@link com.graphaware.algo.path.RelationshipCostFinder} that reads the cost from a relationship's property.
 * In case the property is undefined or not a number, {@link #getDefaultCost()} is called to let subclasses determine
 * the cost.
 */
public abstract class PropertyBasedRelationshipCostFinder implements RelationshipCostFinder {

    private final String costPropertyKey;

    /**
     * Construct a new cost finder.
     * @param costPropertyKey key of the relationship property that defines cost.
     */
    protected PropertyBasedRelationshipCostFinder(String costPropertyKey) {
        this.costPropertyKey = costPropertyKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCost(Relationship relationship) {
        if (!relationship.hasProperty(costPropertyKey)) {
            return getDefaultCost();
        }

        return objectToLong(relationship.getProperty(costPropertyKey));
    }

    private long objectToLong(Object value) {
        if (value instanceof Integer) {
            return new Integer((int) value).longValue();
        }

        if (value instanceof Long) {
            return (long) value;
        }

        return getDefaultCost();
    }

    /**
     * Get the cost of a relationship with undefined cost property.
     *
     * @return default cost.
     */
    protected abstract long getDefaultCost();
}
