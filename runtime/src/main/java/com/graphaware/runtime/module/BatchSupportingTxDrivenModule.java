package com.graphaware.runtime.module;

import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;

/**
 * {@link TxDrivenModule} that can be backed by a {@link TransactionSimulatingBatchInserter} (on top of {@link org.neo4j.graphdb.GraphDatabaseService}).
 *
 * @param <T> The type of a state object that the module can use to
 *            pass information from the {@link #beforeCommit(com.graphaware.tx.event.improved.api.ImprovedTransactionData)}
 *            method to the {@link #afterCommit(Object)} method.
 */
public interface BatchSupportingTxDrivenModule<T> extends TxDrivenModule<T> {

    /**
     * Start the module. This method must bring the module to a usable state, i.e. after it returns, the module must be
     * able to handle multi-threaded requests. This method is guaranteed to be called exactly once every time the runtime/database
     * starts.
     *
     * @param batchInserter to start this module against.
     */
    void start(TransactionSimulatingBatchInserter batchInserter);

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
