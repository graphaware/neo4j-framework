package com.graphaware.runtime.module;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.config.NullRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;

/**
 *  Base class for {@link TransactionDrivenRuntimeModule} implementations.
 */
public abstract class BaseTransactionDrivenRuntimeModule implements TransactionDrivenRuntimeModule {

    private final String moduleId;

    protected BaseTransactionDrivenRuntimeModule(String moduleId) {
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
     * <p>
     * Note that the implementation in this base class doesn't do anything and can be safely overridden without calling super.
     * </p>
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
     * <p>
     * Note that the implementation in this base class doesn't do anything and can be safely overridden without calling super.
     * </p>
     */
    @Override
    public void shutdown() {
        //to be overridden
    }

}
