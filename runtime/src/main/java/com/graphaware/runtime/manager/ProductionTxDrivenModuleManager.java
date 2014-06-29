package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TxDrivenModule;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 */
public class ProductionTxDrivenModuleManager extends BaseTxDrivenModuleManager<TxDrivenModule<?>> {
    private static final Logger LOG = Logger.getLogger(ProductionTxDrivenModuleManager.class);

    private final GraphDatabaseService database;

    public ProductionTxDrivenModuleManager(ModuleMetadataRepository metadataRepository, GraphDatabaseService database) {
        super(metadataRepository);
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInitialize(TxDrivenModule module) {
        module.initialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doReinitialize(TxDrivenModule module) {
        module.reinitialize(database);
    }
}
