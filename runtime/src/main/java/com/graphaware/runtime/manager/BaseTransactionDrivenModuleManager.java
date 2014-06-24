package com.graphaware.runtime.manager;

import com.graphaware.runtime.NeedsInitializationException;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.apache.log4j.Logger;

/**
 *
 */
public abstract class BaseTransactionDrivenModuleManager<T extends TxDrivenModule> extends BaseModuleManager<T> implements TransactionDrivenModuleManager<T> {

    private static final Logger LOG = Logger.getLogger(BaseTransactionDrivenModuleManager.class);

    protected BaseTransactionDrivenModuleManager(ModuleMetadataRepository metadataRepository) {
        super(metadataRepository);
    }

    @Override
    public void beforeCommit(TransactionDataContainer transactionData) {
        for (T module : modules) {
            FilteredTransactionData filteredTransactionData = new FilteredTransactionData(transactionData, module.getConfiguration().getInclusionStrategies());

            if (!filteredTransactionData.mutationsOccurred()) {
                continue;
            }

            try {
                module.beforeCommit(filteredTransactionData);
            } catch (NeedsInitializationException e) {
                LOG.warn("Module " + module.getId() + " seems to have a problem and will be re-initialized next time the database is started. ");
                forceInitialization(module);
            } catch (RuntimeException e) {
                LOG.warn("Module " + module.getId() + " threw an exception!", e);
            }
        }
    }
}
