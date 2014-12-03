package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Map;
import java.util.Queue;

/**
 * {@link ModuleManager} for {@link TxDrivenModule}s.
 */
public interface TxDrivenModuleManager<T extends TxDrivenModule> extends ModuleManager<T> {

    /**
     * Delegate work to modules before a transaction is committed.
     *
     * @param transactionData about-to-be-committed transaction data.
     * @return map of objects (states) returned by the modules, keyed by {@link com.graphaware.runtime.module.TxDrivenModule#getId()}.
     */
    Map<String, Object> beforeCommit(TransactionDataContainer transactionData);

    /**
     * Delegate work to modules after a transaction is committed.
     *
     * @param states returned by {@link #beforeCommit(com.graphaware.tx.event.improved.data.TransactionDataContainer)}.
     */
    void afterCommit(Map<String, Object> states);

    /**
     * Delegate work to modules after a transaction is rolled back.
     *
     * @param states returned by {@link #beforeCommit(com.graphaware.tx.event.improved.data.TransactionDataContainer)}.
     */
    void afterRollback(Map<String, Object> states);
}
