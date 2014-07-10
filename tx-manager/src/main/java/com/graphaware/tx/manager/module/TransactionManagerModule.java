package com.graphaware.tx.manager.module;

import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.tx.manager.TransactionManager;
import com.graphaware.tx.manager.TransactionManagerImpl;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;

public class TransactionManagerModule extends BaseTxDrivenModule<Void> {

    private final TransactionManager transactionMonitor;

    public TransactionManagerModule(String moduleId, GraphDatabaseService database) {
        super(moduleId);
        transactionMonitor = new TransactionManagerImpl(database);
    }

    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) {
        System.out.println("module beforeCommit was called");
        //transactionMonitor.beforeCommit(transactionData);
        return null;
    }
}
