package com.graphaware.runtime.schedule;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.transaction.TxManager;

import static com.graphaware.runtime.config.RuntimeConfiguration.INITIAL_TIMER_DELAY;
import static com.graphaware.runtime.config.RuntimeConfiguration.TIMER_DELAY;

/**
 *
 */
public class AdaptiveTimingStrategy implements TimingStrategy {

    private final TxManager transactionManager;

    public AdaptiveTimingStrategy(GraphDatabaseService database) {
        this.transactionManager = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency( TxManager.class );
    }

    @Override
    public long nextDelay(long lastTaskDuration) {
        if (lastTaskDuration == -2) {
            return INITIAL_TIMER_DELAY;
        }
        System.out.println("Committed tx: " + transactionManager.getCommittedTxCount());
        return TIMER_DELAY;
    }
}
