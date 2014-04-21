package com.graphaware.runtime.bootstrap;

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.GraphAwareRuntimeModule;
import com.graphaware.runtime.config.NullRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntimeModule} that can tell whether it has been initialized for testing.
 */
public class TestRuntimeModule implements GraphAwareRuntimeModule {

    public static final List<TestRuntimeModule> TEST_RUNTIME_MODULES = new ArrayList<>();

    private final String id;
    private final Map<String, String> config;
    private boolean initialized = false;

    public TestRuntimeModule(String id, Map<String, String> config) {
        this.id = id;
        this.config = config;
        TEST_RUNTIME_MODULES.add(this);
    }

    public String getId() {
        return id;
    }

    @Override
    public RuntimeModuleConfiguration getConfiguration() {
        return NullRuntimeModuleConfiguration.getInstance();
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public boolean isInitialized() {
        return initialized;
    }


    @Override
    public void initialize(GraphDatabaseService database) {
        try {
            Thread.sleep(200); //takes some time to initialize
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
        //do nothing
    }
}
