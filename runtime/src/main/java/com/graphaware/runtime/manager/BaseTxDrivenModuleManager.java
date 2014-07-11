package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.NeedsInitializationException;
import com.graphaware.runtime.metadata.DefaultTxDrivenModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.event.TransactionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link BaseModuleManager} for {@link TxDrivenModule}s.
 */
public abstract class BaseTxDrivenModuleManager<T extends TxDrivenModule> extends BaseModuleManager<TxDrivenModuleMetadata, T> implements TxDrivenModuleManager<T> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTxDrivenModuleManager.class);

    /**
     * Construct a new manager.
     *
     * @param metadataRepository repository for storing module metadata.
     */
    protected BaseTxDrivenModuleManager(ModuleMetadataRepository metadataRepository) {
        super(metadataRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void throwExceptionIfIllegal(TransactionData transactionData) {
        metadataRepository.throwExceptionIfIllegal(transactionData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleCorruptMetadata(T module) {
        LOG.info("Module " + module.getId() + " seems to have corrupted metadata, will re-initialize...");
        reinitialize(module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleNoMetadata(T module) {
        LOG.info("Module " + module.getId() + " seems to have been registered for the first time, will initialize...");
        initialize(module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TxDrivenModuleMetadata createFreshMetadata(T module) {
        return new DefaultTxDrivenModuleMetadata(module.getConfiguration());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TxDrivenModuleMetadata acknowledgeMetadata(T module, TxDrivenModuleMetadata metadata) {
        if (metadata.needsInitialization()) {
            LOG.info("Module " + module.getId() + " has been marked for re-initialization on " + new Date(metadata.problemTimestamp()).toString() + ". Will re-initialize...");
            reinitialize(module);
            return createFreshMetadata(module);
        }

        if (!metadata.getConfig().equals(module.getConfiguration())) {
            LOG.info("Module " + module.getId() + " seems to have changed configuration since last run, will re-initialize...");
            reinitialize(module);
            return createFreshMetadata(module);
        }

        LOG.info("Module " + module.getId() + " has not changed configuration since last run, already initialized.");
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startModules() {
        LOG.info("Starting transaction-driven modules...");
        for (T module : modules) {
            start(module);
        }
        LOG.info("Transaction-driven modules started.");
    }

    /**
     * Start module. This means preparing for doing the actual work. Call in a single-thread exactly once on each module
     * every time the runtime starts.
     *
     * @param module to be started.
     */
    protected abstract void start(T module);

    /**
     * Initialize module. This means doing any work necessary for a module that has been registered for the first time
     * on an existing database, or that has been previously registered with different configuration.
     * <p/>
     * For example, a module that performs some in-graph caching needs to write information into the graph so that when
     * the method returns, the graph is in the same state as it would be if the module has been running all the time
     * since the graph was empty.
     * <p/>
     * Note that for many modules, it might not be necessary to do anything.
     *
     * @param module to initialize.
     */
    protected abstract void initialize(T module);

    /**
     * Re-initialize module. This means cleaning up all data this module might have ever written to the graph and
     * doing any work necessary for a module that has been registered for the first time
     * on an existing database, or that has been previously registered with different configuration.
     * <p/>
     * For example, a module that performs some in-graph caching needs to write information into the graph so that when
     * the method returns, the graph is in the same state as it would be if the module has been running all the time
     * since the graph was empty.
     * <p/>
     * Note that for many modules, it might not be necessary to do anything.
     *
     * @param module to initialize.
     */
    protected abstract void reinitialize(T module);

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> beforeCommit(TransactionDataContainer transactionData) {
        Map<String, Object> result = new HashMap<>();

        for (T module : modules) {
            FilteredTransactionData filteredTransactionData = new FilteredTransactionData(transactionData, module.getConfiguration().getInclusionStrategies());

            if (!filteredTransactionData.mutationsOccurred()) {
                continue;
            }

            Object state = null;

            try {
                state = module.beforeCommit(filteredTransactionData);
            } catch (NeedsInitializationException e) {
                LOG.warn("Module " + module.getId() + " seems to have a problem and will be re-initialized next time the database is started. ");
                TxDrivenModuleMetadata moduleMetadata = metadataRepository.getModuleMetadata(module);
                metadataRepository.persistModuleMetadata(module, moduleMetadata.markedNeedingInitialization());
            } catch (DeliberateTransactionRollbackException e) {
                LOG.debug("Module " + module.getId() + " threw an exception indicating that the transaction should be rolled back.", e);

                result.put(module.getId(), state);      //just so the module gets afterRollback called as well
                afterRollback(result); //remove this when https://github.com/neo4j/neo4j/issues/2660 is resolved

                throw e;               //will cause rollback
            } catch (RuntimeException e) {
                LOG.warn("Module " + module.getId() + " threw an exception", e);
            }

            result.put(module.getId(), state);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(Map<String, Object> states) {
        for (T module : modules) {
            if (!states.containsKey(module.getId())) {
                return; //perhaps module wasn't interested, or threw RuntimeException
            }

            module.afterCommit(states.get(module.getId()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterRollback(Map<String, Object> states) {
        for (T module : modules) {
            if (!states.containsKey(module.getId())) {
                return; //rollback happened before this module had a go
            }

            module.afterRollback(states.get(module.getId()));
        }
    }
}
