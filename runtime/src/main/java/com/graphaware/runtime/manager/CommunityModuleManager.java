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
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.runtime.module.Module;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import java.util.HashMap;
import java.util.Map;

public class CommunityModuleManager extends BaseModuleManager implements ModuleManager {

    private static final Log LOG = LoggerFactory.getLogger(CommunityModuleManager.class);

    private final GraphDatabaseService database;

    /**
     * Construct a new manager.
     *
     * @param database database.
     */
    public CommunityModuleManager(GraphDatabaseService database) {
        super();
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startModules() {
        super.startModules();

        if (modules.isEmpty()) {
            LOG.info("No GraphAware Runtime modules registered.");
            return;
        }

        LOG.info("Starting GraphAware Runtime modules...");
        for (Module<?> module : modules.values()) {
            start(module);
        }
        LOG.info("GraphAware Runtime modules started.");
    }

    /**
     * Start module. This means preparing for doing the actual work. Call in a single-thread exactly once on each module
     * every time the runtime starts.
     *
     * @param module to be started.
     */
    protected void start(Module<?> module) {
        module.start(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> beforeCommit(TransactionDataContainer transactionData) {
        Map<String, Object> result = new HashMap<>();

        for (Module<?> module : modules.values()) {
            FilteredTransactionData filteredTransactionData = new FilteredTransactionData(transactionData, transactionData.getTransaction(), module.getConfiguration().getInclusionPolicies());

            if (!filteredTransactionData.mutationsOccurred()) {
                continue;
            }

            Object state = null;

            try {
                state = module.beforeCommit(filteredTransactionData);
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

    private Map<String, Object> handleException(Map<String, Object> result, Module<?> module, Object state, RuntimeException e) {
        result.put(module.getId(), state);      //just so the module gets afterRollback called as well
        afterRollback(result); //remove this when https://github.com/neo4j/neo4j/issues/2660 is resolved (todo this is fixed in 3.3)
        throw e;               //will cause rollback
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCommit(Map<String, Object> states) {
        for (Module module : modules.values()) {
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
        for (Module module : modules.values()) {
            if (!states.containsKey(module.getId())) {
                return; //rollback happened before this module had a go
            }

            module.afterRollback(states.get(module.getId()));
        }
    }
}
