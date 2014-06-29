package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.strategy.BatchSupportingTxDrivenModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;

/**
 *
 */
public class BatchModuleManager extends BaseTxDrivenModuleManager<BatchSupportingTxDrivenModule<?>> {

    private final TransactionSimulatingBatchInserter batchInserter;

    public BatchModuleManager(ModuleMetadataRepository metadataRepository, TransactionSimulatingBatchInserter batchInserter) {
        super(metadataRepository);
        this.batchInserter = batchInserter;
    }

    @Override
    protected void doInitialize(BatchSupportingTxDrivenModule module) {
        module.initialize(batchInserter);
    }

    @Override
    protected void doReinitialize(BatchSupportingTxDrivenModule module) {
        module.reinitialize(batchInserter);
    }
}
