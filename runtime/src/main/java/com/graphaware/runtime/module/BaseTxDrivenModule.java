package com.graphaware.runtime.module;

import com.graphaware.runtime.config.NullTxDrivenModuleConfiguration;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Base class for {@link TxDrivenModule} implementations.
 *
 * @param <T> The type of a state object that the module can use to
 *            pass information from the {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}
 *            method to the {@link #afterCommit(Object)} method.
 */
public abstract class BaseTxDrivenModule<T> extends BaseRuntimeModule implements TxDrivenModule<T> {

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module.
     */
    protected BaseTxDrivenModule(String moduleId) {
        super(moduleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxDrivenModuleConfiguration getConfiguration() {
        return NullTxDrivenModuleConfiguration.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(GraphDatabaseService database) {
        //to be overridden
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(T state) {
        //allow subclasses to override
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterRollback(T state) {
        //allow subclasses to override
    }
}
