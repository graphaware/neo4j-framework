package com.graphaware.runtime.manager;

import com.graphaware.runtime.NeedsInitializationException;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TransactionDrivenRuntimeModule;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 */
public class ProductionTransactionDrivenModuleManager extends BaseTransactionDrivenModuleManager<TransactionDrivenRuntimeModule>  {
    private static final Logger LOG = Logger.getLogger(ProductionTransactionDrivenModuleManager.class);

    private final GraphDatabaseService database;

    public ProductionTransactionDrivenModuleManager(ModuleMetadataRepository metadataRepository, GraphDatabaseService database) {
        super(metadataRepository);
        this.database = database;
    }

    @Override
    protected void doInitialize(TransactionDrivenRuntimeModule module) {
        module.initialize(database);
    }

    @Override
    protected void doReinitialize(TransactionDrivenRuntimeModule module) {
        module.reinitialize(database);
    }
}
