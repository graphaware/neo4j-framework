package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;

import org.neo4j.graphdb.event.TransactionData;

/**
 *
 */
public interface TransactionDrivenModuleManager<T extends TxDrivenModule> extends ModuleManager<T> {

    /**
     * Throw an exception if the transaction that's about to be committed does something illegal from the manager's
     * point of view. This is a performance optimization.
     *
     * @param transactionData about-to-be-committed transaction data.
     * @throws IllegalStateException if the transaction is illegal.
     */
    void throwExceptionIfIllegal(TransactionData transactionData);

    void beforeCommit(TransactionDataContainer transactionData);
}
