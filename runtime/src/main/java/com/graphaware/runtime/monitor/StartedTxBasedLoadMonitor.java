package com.graphaware.runtime.monitor;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.transaction.xaframework.TransactionCounters;

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
