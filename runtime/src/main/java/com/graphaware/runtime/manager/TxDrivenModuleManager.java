package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.event.TransactionData;

/**
 * {@link ModuleManager} for {@link TxDrivenModule}s.
 */
public interface TxDrivenModuleManager<T extends TxDrivenModule> extends ModuleManager<T> {

    /**
     * Throw an exception if the transaction that's about to be committed does something illegal from the manager's
     * point of view.
     * <p/>
     * Note that the same thing could be done in the {@link #beforeCommit(com.graphaware.tx.event.improved.data.TransactionDataContainer)}
     * method. This is a performance optimization to avoid translating {@link TransactionData} into {@link TransactionDataContainer}
     * when the transaction is illegal anyway.
     *
     * @param transactionData about-to-be-committed transaction data.
     * @throws IllegalStateException if the transaction is illegal.
     */
    void throwExceptionIfIllegal(TransactionData transactionData);

    /**
     * Do the work that is the raison d'etre of this module. This could be logging, writing additional things
     * into the graph, preventing the transaction from happening by throwing a {@link RuntimeException}, etc.
     *
     * @param transactionData about-to-be-committed transaction data.
     */
    void beforeCommit(TransactionDataContainer transactionData);
}
