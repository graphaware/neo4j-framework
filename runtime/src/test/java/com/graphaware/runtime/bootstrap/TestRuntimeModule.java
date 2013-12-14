package com.graphaware.runtime.bootstrap;

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.GraphAwareRuntimeModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntimeModule} that can tell whether it has been initialized for testing.
 */
public class TestRuntimeModule implements GraphAwareRuntimeModule {

    private static boolean initialized = false;

    public static boolean isInitialized() {
        return initialized;
    }

    public static void reset() {
        initialized = false;
    }

    @Override
    public String getId() {
        return "test";
    }

    @Override
    public void initialize(GraphDatabaseService database) {
        try {
            Thread.sleep(1000); //takes some time to initialize
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initialized = true;
    }

    @Override
    public void reinitialize(GraphDatabaseService database) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize(TransactionSimulatingBatchInserter batchInserter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reinitialize(TransactionSimulatingBatchInserter batchInserter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
        initialized = false;
    }

    @Override
    public void beforeCommit(ImprovedTransactionData transactionData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InclusionStrategies getInclusionStrategies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String asString() {
        return "testModule";
    }
}
