/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.manager;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.ping.StatsCollector;
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import com.graphaware.runtime.metadata.DefaultTxDrivenModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TxDrivenModuleMetadata;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.NeedsInitializationException;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.logging.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link BaseModuleManager} for {@link TxDrivenModule}s.
 */
public abstract class BaseTxDrivenModuleManager<T extends TxDrivenModule> extends BaseModuleManager<TxDrivenModuleMetadata, T> implements TxDrivenModuleManager<T> {

    private static final Log LOG = LoggerFactory.getLogger(BaseTxDrivenModuleManager.class);

    private final InstanceRoleUtils instanceRoleUtils;

    /**
     * Construct a new manager.
     *
     * @param metadataRepository repository for storing module metadata.
     * @param statsCollector     stats collector.
     * @param instanceRoleUtils  instance role utils.
     */
    protected BaseTxDrivenModuleManager(ModuleMetadataRepository metadataRepository, StatsCollector statsCollector, InstanceRoleUtils instanceRoleUtils) {
        super(metadataRepository, statsCollector);
        this.instanceRoleUtils = instanceRoleUtils;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleCorruptMetadata(T module) {
        LOG.info("Module " + module.getId() + " seems to have corrupted metadata, will try to re-initialize...");
        reinitializeIfAllowed(module, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleNoMetadata(T module) {
        LOG.info("Module " + module.getId() + " seems to have been registered for the first time, will try to initialize...");
        initializeIfAllowed(module);
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
            LOG.info("Module " + module.getId() + " has been marked for re-initialization on " + new Date(metadata.problemTimestamp()).toString() + ". Will try to re-initialize...");
            reinitializeIfAllowed(module, metadata);
            return createFreshMetadata(module);
        }

        if (!metadata.getConfig().equals(module.getConfiguration())) {
            LOG.info("Module " + module.getId() + " seems to have changed configuration since last run, will try to re-initialize...");
            reinitializeIfAllowed(module, metadata);
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
        super.startModules();

        LOG.info("Starting transaction-driven modules...");
        for (T module : modules.values()) {
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

    private void initializeIfAllowed(T module) {
        if (allowedToInitialize(module, "initialize")) {
            initialize(module);
        }
    }

    private void reinitializeIfAllowed(T module, TxDrivenModuleMetadata metadata) {
        if (allowedToInitialize(module, "re-initialize")) {
            reinitialize(module, metadata);
        }
    }

    private boolean allowedToInitialize(T module, String logMessage) {
        if (instanceRoleUtils.getInstanceRole().isReadOnly()) {
            LOG.info("Instance not writable. Will NOT " + logMessage + ".");
            return false;
        }

        long initUntil = module.getConfiguration().initializeUntil();
        long now = System.currentTimeMillis();

        if (initUntil > now) {
            LOG.info("InitializeUntil set to " + initUntil + " and it is " + now + ". Will " + logMessage + ".");
            return true;
        } else {
            LOG.info("InitializeUntil set to " + initUntil + " and it is " + now + ". Will NOT " + logMessage + ".");
            return false;
        }
    }

    /**
     * Initialize module. This means doing any work necessary for a module that has been registered for the first time
     * on an existing database, or that has been previously registered with different configuration.
     * <p>
     * For example, a module that performs some in-graph caching needs to write information into the graph so that when
     * the method returns, the graph is in the same state as it would be if the module has been running all the time
     * since the graph was empty.
     * <p>
     * Note that for many modules, it might not be necessary to do anything.
     *
     * @param module to initialize.
     */
    protected abstract void initialize(T module);

    /**
     * Re-initialize module. This means cleaning up all data this module might have ever written to the graph and
     * doing any work necessary for a module that has been registered for the first time
     * on an existing database, or that has been previously registered with different configuration.
     * <p>
     * For example, a module that performs some in-graph caching needs to write information into the graph so that when
     * the method returns, the graph is in the same state as it would be if the module has been running all the time
     * since the graph was empty.
     * <p>
     * Note that for many modules, it might not be necessary to do anything.
     *
     * @param module      to initialize.
     * @param oldMetadata metadata stored for this module from its previous run. Can be <code>null</code> in case metadata
     *                    was corrupt or there was no metadata.
     */
    protected abstract void reinitialize(T module, TxDrivenModuleMetadata oldMetadata);

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> beforeCommit(TransactionDataContainer transactionData) {
        Map<String, Object> result = new HashMap<>();

        for (T module : modules.values()) {
            FilteredTransactionData filteredTransactionData = new FilteredTransactionData(transactionData, module.getConfiguration().getInclusionPolicies());

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
                return handleException(result, module, state, e);
            } catch (RuntimeException e) {
                LOG.warn("Module " + module.getId() + " threw an exception", e);
                return handleException(result, module, state, e);
            }

            result.put(module.getId(), state);
        }

        return result;
    }

    private Map<String, Object> handleException(Map<String, Object> result, T module, Object state, RuntimeException e) {
        result.put(module.getId(), state);      //just so the module gets afterRollback called as well
        afterRollback(result); //remove this when https://github.com/neo4j/neo4j/issues/2660 is resolved (todo this is fixed in 3.3)
        throw e;               //will cause rollback
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(Map<String, Object> states) {
        for (T module : modules.values()) {
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
        for (T module : modules.values()) {
            if (!states.containsKey(module.getId())) {
                return; //rollback happened before this module had a go
            }

            module.afterRollback(states.get(module.getId()));
        }
    }
}
