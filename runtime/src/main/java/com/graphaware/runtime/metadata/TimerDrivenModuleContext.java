/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

/**
 * Context of {@link com.graphaware.runtime.module.TimerDrivenModule}s encapsulating the position in the graph and additional
 * data, such as weight carried around for certain iterative algorithms, etc.
 * <p>
 * The position in the graph could be a single node, a collection of nodes, a single relationship, a collection of
 * relationships, a cluster of nodes and relationships, etc.
 * </p>
 *
 * @param <T> type of the position representation.
 */
public interface TimerDrivenModuleContext<T> {

    public static final long ASAP = -1;

    /**
     * Get the earliest time the {@link com.graphaware.runtime.module.TimerDrivenModule} wishes to be called again.
     * Modules that want to be called as often as possible should return {@link #ASAP}.
     *
     * @return earliest time (in ms since 1/1/1970) this module will be called again.
     */
    long earliestNextCall();

    /**
     * Find the position in the database.
     *
     * @param database The {@link GraphDatabaseService} in which to find the position.
     * @return A representation of the position.
     * @throws PositionNotFoundException if the position could not be found. Callers must handle this.
     */
    T find(GraphDatabaseService database) throws PositionNotFoundException;
}
