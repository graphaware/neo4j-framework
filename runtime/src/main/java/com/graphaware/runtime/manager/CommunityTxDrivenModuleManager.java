/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
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
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link BaseModuleManager} for {@link TxDrivenModule}s.
 */
public class CommunityTxDrivenModuleManager<T extends TxDrivenModule> extends BaseModuleManager<T> implements TxDrivenModuleManager<T> {

    private static final Log LOG = LoggerFactory.getLogger(CommunityTxDrivenModuleManager.class);

    private final GraphDatabaseService database;

    /**
     * Construct a new manager.
     *
     * @param database           database.
     * @param statsCollector     stats collector.
     */
    public CommunityTxDrivenModuleManager(GraphDatabaseService database, StatsCollector statsCollector) {
        super(statsCollector);
        this.database = database;
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
    protected void start(TxDrivenModule module) {
        module.start(database);
    }

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
            }  catch (DeliberateTransactionRollbackException e) {
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
