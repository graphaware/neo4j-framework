package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.TransactionDrivenRuntimeModule;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.event.TransactionData;

/**
 *
 */
public interface TransactionDrivenModuleManager<T extends TransactionDrivenRuntimeModule> extends ModuleManager<T> {

    void throwExceptionIfIllegal(TransactionData transactionData);

    void beforeCommit(TransactionDataContainer transactionData);
}
