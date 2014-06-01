package com.graphaware.runtime.strategy;

import com.graphaware.runtime.GraphAwareRuntimeModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;

/**
 *
 */
public interface BatchSupportingGraphAwareRuntimeModule extends GraphAwareRuntimeModule {

    /**
     * Initialize this module. This method must bring the module to a state equivalent to a state of the same module that
     * has been registered at all times since the database was empty. It can perform global-graph operations to achieve
     * this.
     *
     * @param batchInserter to initialize this module for.
     */
    void initialize(TransactionSimulatingBatchInserter batchInserter);

    /**
     * Re-initialize this module. This method must remove all metadata written to the graph by this module and bring the
     * module to a state equivalent to a state of the same module that has been registered at all times since the
     * database was empty. It can perform global-graph operations to achieve this.
     *
     * @param batchInserter to initialize this module for.
     */
    void reinitialize(TransactionSimulatingBatchInserter batchInserter);
}
