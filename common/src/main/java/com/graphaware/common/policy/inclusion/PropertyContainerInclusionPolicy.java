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


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

/**
 * {@link ObjectInclusionPolicy} for {@link PropertyContainer}s.
 */
public interface PropertyContainerInclusionPolicy<T extends PropertyContainer> extends ObjectInclusionPolicy<T> {

    /**
     * Get all {@link PropertyContainer}s matching the policy from the database. This can be a naive implementation
     * retrieving all {@link PropertyContainer}s and calling the {@link #include(Object)} method for each one of them,
     * but it could also be more clever than that and directly retrieve the included {@link PropertyContainer}s from an
     * index, by label, etc.
     *
     * @param database to retrieve the {@link PropertyContainer}s from.
     * @return all {@link PropertyContainer}s matching the policy.
     */
    Iterable<T> getAll(GraphDatabaseService database);
}
