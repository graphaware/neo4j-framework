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

package com.graphaware.runtime.metadata;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

/**
 * A {@link TimerDrivenModuleContext} that is empty, i.e. holds no information about the module's position in the graph.
 */
public final class EmptyContext extends BaseTimerDrivenModuleContext<Void> {

    /**
     * Construct an empty context requesting that the module be called again ASAP.
     */
    public EmptyContext() {
        this(ASAP);
    }

    /**
     * Construct an empty context requesting that the module be called again earliest at a specified point in time.
     *
     * @param earliestNextCall time in ms since 1/1/1970 when the module should be called again at the earliest.
     */
    public EmptyContext(long earliestNextCall) {
        super(earliestNextCall);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void find(GraphDatabaseService database) throws NotFoundException {
        throw new UnsupportedOperationException("Empty context holds no information about the module's position in the graph");
    }
}
