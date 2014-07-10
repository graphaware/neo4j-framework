package com.graphaware.tx.manager.module;

import com.graphaware.tx.manager.TransactionManager;
import com.graphaware.tx.manager.TransactionManagerImpl;
import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;

public class TransactionManagerModule extends BaseGraphAwareRuntimeModule  {

    private final TransactionManager transactionMonitor;

    public TransactionManagerModule(String moduleId, GraphDatabaseService database) {
        super(moduleId);
        transactionMonitor = new TransactionManagerImpl(database);
    }

    @Override
    public void beforeCommit(ImprovedTransactionData transactionData) {
        System.out.println("module beforeCommit was called");
        //transactionMonitor.beforeCommit(transactionData);
    }
}
