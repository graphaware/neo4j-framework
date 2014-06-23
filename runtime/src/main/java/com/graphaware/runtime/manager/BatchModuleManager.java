package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.strategy.BatchSupportingTransactionDrivenRuntimeModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;

/**
 *
 */
public class BatchModuleManager extends BaseTransactionDrivenModuleManager<BatchSupportingTransactionDrivenRuntimeModule> {

    private final TransactionSimulatingBatchInserter batchInserter;

    public BatchModuleManager(ModuleMetadataRepository metadataRepository, TransactionSimulatingBatchInserter batchInserter) {
        super(metadataRepository);
        this.batchInserter = batchInserter;
    }

    @Override
    protected void doInitialize(BatchSupportingTransactionDrivenRuntimeModule module) {
        module.initialize(batchInserter);
    }

    @Override
    protected void doReinitialize(BatchSupportingTransactionDrivenRuntimeModule module) {
        module.reinitialize(batchInserter);
    }
}
