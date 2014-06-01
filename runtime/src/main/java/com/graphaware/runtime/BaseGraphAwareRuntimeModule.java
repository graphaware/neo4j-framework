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
    private final GraphDatabaseService database;

    protected BaseGraphAwareRuntimeModule(String moduleId, GraphDatabaseService database) {
        this.moduleId = moduleId;
        this.database = database;
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
    public GraphDatabaseService getDatabase() {
        return database;
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
        //to be overridden if re-initialisation differs from initialisation
        initialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //to be overridden
    }
}
