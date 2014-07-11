package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.BatchSupportingTxDrivenModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;

/**
 * {@link BaseTxDrivenModuleManager} backed by a {@link TransactionSimulatingBatchInserter}.
 * <p/>
 * Only supports {@link BatchSupportingTxDrivenModule}s.
 */
public class BatchModuleManager extends BaseTxDrivenModuleManager<BatchSupportingTxDrivenModule> {

    private final TransactionSimulatingBatchInserter batchInserter;

    /**
     * Create a new manager.
     *
     * @param batchInserter      that stores graph data.
     * @param metadataRepository for storing module metadata.
     */
    public BatchModuleManager(TransactionSimulatingBatchInserter batchInserter, ModuleMetadataRepository metadataRepository) {
        super(metadataRepository);
        this.batchInserter = batchInserter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void start(BatchSupportingTxDrivenModule module) {
        module.start(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(BatchSupportingTxDrivenModule module) {
        module.initialize(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinitialize(BatchSupportingTxDrivenModule module) {
        module.reinitialize(batchInserter);
    }
}
