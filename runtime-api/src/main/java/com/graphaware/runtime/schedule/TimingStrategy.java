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

package com.graphaware.runtime.schedule;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A strategy for timing scheduled tasks.
 */
public interface TimingStrategy {

    public static final int UNKNOWN = -1;
    public static final int NEVER_RUN = -2;

    /**
     * Initialize the timing strategy before it can be used.
     *
     * @param database against which the runtime using this strategy runs.
     */
    void initialize(GraphDatabaseService database);

    /**
     * Get the delay until the timer fires again.
     *
     * @param lastTaskDuration The total time it took to execute the last task in milliseconds. -1 if unknown.
     * @return The next delay in <b>milliseconds</b>.
     */
    long nextDelay(long lastTaskDuration);

}
