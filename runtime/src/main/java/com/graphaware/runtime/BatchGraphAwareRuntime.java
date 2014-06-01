/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.runtime.strategy.BatchSupportingGraphAwareRuntimeModule;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

import java.util.Collection;
import java.util.HashMap;

import static com.graphaware.runtime.config.RuntimeConfiguration.GA_ROOT;


/**
 * {@link GraphAwareRuntime} that operates on a {@link org.neo4j.unsafe.batchinsert.BatchInserter}
 * (or more precisely {@link TransactionSimulatingBatchInserter}) rather than {@link org.neo4j.graphdb.GraphDatabaseService}.
 *
 * @see BaseGraphAwareRuntime
 * @see org.neo4j.unsafe.batchinsert.BatchInserter - same limitations apply.
 */
public class BatchGraphAwareRuntime extends BaseGraphAwareRuntime {
    private static final Logger LOG = Logger.getLogger(BatchGraphAwareRuntime.class);

    private final TransactionSimulatingBatchInserter batchInserter;

    /**
     * Create a new instance of the runtime.
     *
     * @param batchInserter that the runtime should use.
     */
    public BatchGraphAwareRuntime(TransactionSimulatingBatchInserter batchInserter) {
        super();
        this.batchInserter = batchInserter;
        registerSelfAsHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSelfAsHandler() {
        batchInserter.registerTransactionEventHandler(this);
        batchInserter.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean databaseAvailable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Transaction startTransaction() {
        return new Transaction() {
            @Override
            public void failure() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public void success() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public void finish() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public void close() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public Lock acquireWriteLock(PropertyContainer entity) {
                throw new UnsupportedOperationException("Fake tx!");
            }

            @Override
            public Lock acquireReadLock(PropertyContainer entity) {
                throw new UnsupportedOperationException("Fake tx!");
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInitialize(GraphAwareRuntimeModule module) {
        if (module instanceof BatchSupportingGraphAwareRuntimeModule) {
            ((BatchSupportingGraphAwareRuntimeModule) module).initialize(batchInserter);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doReinitialize(GraphAwareRuntimeModule module) {
        if (module instanceof BatchSupportingGraphAwareRuntimeModule) {
            ((BatchSupportingGraphAwareRuntimeModule) module).reinitialize(batchInserter);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRecordInitialization(final GraphAwareRuntimeModule module, final String key) {
        getOrCreateRoot().setProperty(key, Serializer.toString(module.getConfiguration(), CONFIG));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeUnusedModules(final Collection<String> unusedModules) {
        for (String toRemove : unusedModules) {
            LOG.info("Removing unused module " + toRemove + ".");
            getOrCreateRoot().removeProperty(toRemove);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forceInitialization(final GraphAwareRuntimeModule module) {
        getOrCreateRoot().setProperty(moduleKey(module), FORCE_INITIALIZATION + System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getOrCreateRoot() {
        for (long candidate : batchInserter.getAllNodes()) {
            if (batchInserter.nodeHasLabel(candidate, GA_ROOT)) {
                return new BatchInserterNode(candidate, batchInserter);
            }
        }

        return new BatchInserterNode(batchInserter.createNode(new HashMap<String, Object>(), GA_ROOT), batchInserter);
    }
}
