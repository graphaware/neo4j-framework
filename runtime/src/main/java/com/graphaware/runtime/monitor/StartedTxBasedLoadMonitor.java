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

package com.graphaware.runtime.monitor;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.transaction.TransactionCounters;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

/**
 * {@link DatabaseLoadMonitor} returning the database load based on the number of transactions started in a period of time.
 * <p/>
 * The load is measured as the average load in a configurable {@link RunningWindowAverage}.
 * <p/>
 * Samples are taken as the monitor is queried.
 */
public class StartedTxBasedLoadMonitor implements DatabaseLoadMonitor {

    private final TransactionCounters txCounters;
    private final RunningWindowAverage runningWindowAverage;

    /**
     * Construct a new monitor.
     *
     * @param database             to monitor.
     * @param runningWindowAverage to use for the monitoring.
     */
    public StartedTxBasedLoadMonitor(GraphDatabaseService database, RunningWindowAverage runningWindowAverage) {
        this.txCounters = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(TransactionCounters.class);
        this.runningWindowAverage = runningWindowAverage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLoad() {
        runningWindowAverage.sample(System.currentTimeMillis(), txCounters.getNumberOfStartedTransactions());
        return runningWindowAverage.getAverage();
    }
}
