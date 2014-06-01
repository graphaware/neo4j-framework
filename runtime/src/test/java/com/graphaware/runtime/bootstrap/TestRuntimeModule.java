package com.graphaware.runtime.bootstrap;

import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
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
public class TestRuntimeModule extends BaseGraphAwareRuntimeModule {

    public static final List<TestRuntimeModule> TEST_RUNTIME_MODULES = new ArrayList<>();

    private final Map<String, String> config;
    private boolean initialized = false;

    public TestRuntimeModule(String moduleId, Map<String, String> config) {
        super(moduleId);
        this.config = config;
        TEST_RUNTIME_MODULES.add(this);
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
    public void shutdown() {
        initialized = false;
    }

    @Override
    public void beforeCommit(ImprovedTransactionData transactionData) {
        //do nothing
    }
}
