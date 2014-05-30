package com.graphaware.runtime;

import com.graphaware.runtime.config.NullRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *  Base class for {@link GraphAwareRuntimeModule} implementations.
 */
public abstract class BaseGraphAwareRuntimeModule implements GraphAwareRuntimeModule {

    private final String moduleId;

    public BaseGraphAwareRuntimeModule(String moduleId) {
        this.moduleId = moduleId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return moduleId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeModuleConfiguration getConfiguration() {
        return NullRuntimeModuleConfiguration.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        //to be overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reinitialize(GraphDatabaseService database) {
        //to be overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(TransactionSimulatingBatchInserter batchInserter) {
        //to be overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reinitialize(TransactionSimulatingBatchInserter batchInserter) {
        //to be overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //to be overridden
    }
}
