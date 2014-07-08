package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BaseTxDrivenModuleManager} backed by a {@link GraphDatabaseService}.
 */
public class ProductionTxDrivenModuleManager extends BaseTxDrivenModuleManager<TxDrivenModule> {
    private static final Logger LOG = LoggerFactory.getLogger(ProductionTxDrivenModuleManager.class);

    private final GraphDatabaseService database;

    /**
     * Construct a new manager.
     *
     * @param database           storing graph data.
     * @param metadataRepository for storing module metadata.
     */
    public ProductionTxDrivenModuleManager(GraphDatabaseService database, ModuleMetadataRepository metadataRepository) {
        super(metadataRepository);
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(TxDrivenModule module) {
        module.initialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinitialize(TxDrivenModule module) {
        module.reinitialize(database);
    }
}
