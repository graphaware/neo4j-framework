package com.graphaware.runtime.manager;

import com.graphaware.runtime.NeedsInitializationException;
import com.graphaware.runtime.metadata.DefaultTxDrivenModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 *
 */
public abstract class BaseTransactionDrivenModuleManager<T extends TxDrivenModule> extends BaseModuleManager<T> implements TransactionDrivenModuleManager<T> {

    private static final Logger LOG = Logger.getLogger(BaseTransactionDrivenModuleManager.class);

    protected BaseTransactionDrivenModuleManager(ModuleMetadataRepository metadataRepository) {
        super(metadataRepository);
    }

    protected void initializeModule2(T module) {
        TxDrivenModuleMetadata moduleMetadata = metadataRepository.getModuleMetadata(module);

        if (moduleMetadata == null) {
            LOG.info("Module " + module.getId() + " seems to have been registered for the first time, will initialize...");
            initializeModule(module);
            return;
        }

        if (moduleMetadata.needsInitialization()) {
            LOG.info("Module " + module.getId() + " has been marked for re-initialization on "
                    + new Date(moduleMetadata.timestamp()).toString() + ". Will re-initialize...");
            reinitializeModule(module);
            return;
        }

        if (!moduleMetadata.getConfig().equals(module.getConfiguration())) {
            LOG.info("Module " + module.getId() + " seems to have changed configuration since last run, will re-initialize...");
            reinitializeModule(module);
        } else {
            LOG.info("Module " + module.getId() + " has not changed configuration since last run, already initialized.");
        }
    }

    /**
     * Initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void initializeModule(final T module) {
        doInitialize(module);
        recordInitialization(module);
    }

    /**
     * Initialize module.
     *
     * @param module to initialize.
     */
    protected abstract void doInitialize(T module);

    /**
     * Re-initialize a module and capture that fact on the as a root node's property.
     *
     * @param module to initialize.
     */
    private void reinitializeModule(final T module) {
        doReinitialize(module);
        recordInitialization(module);
    }

    /**
     * Re-initialize a module.
     *
     * @param module to initialize.
     */
    protected abstract void doReinitialize(T module);


    /**
     * Capture the fact the a module has been (re-)initialized as a root node's property.
     *
     * @param module that has been initialized.
     */
    private void recordInitialization(final T module) {
        metadataRepository.persistModuleMetadata(module, new DefaultTxDrivenModuleMetadata(module.getConfiguration()));
    }

    /**
     * {@inheritDoc}
     */
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
                metadataRepository.persistModuleMetadata(module, metadataRepository.getModuleMetadata(module).markedNeedingInitialization());
            } catch (RuntimeException e) {
                LOG.warn("Module " + module.getId() + " threw an exception!", e);
            }
        }
    }
}
