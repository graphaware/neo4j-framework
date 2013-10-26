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

package com.graphaware.framework;

import com.graphaware.framework.config.FrameworkConfiguration;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import com.graphaware.tx.event.batch.propertycontainer.inserter.BatchInserterNode;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;

import java.util.Collection;


/**
 * {@link BaseGraphAwareFramework} that operates on a {@link org.neo4j.unsafe.batchinsert.BatchInserter}
 * (or more precisely {@link TransactionSimulatingBatchInserter}) rather than {@link org.neo4j.graphdb.GraphDatabaseService}.
 *
 * @see BaseGraphAwareFramework
 * @see org.neo4j.unsafe.batchinsert.BatchInserter - same limitations apply.
 */
public class BatchGraphAwareFramework extends BaseGraphAwareFramework {
    private static final Logger LOG = Logger.getLogger(BatchGraphAwareFramework.class);

    private final TransactionSimulatingBatchInserter batchInserter;

    /**
     * Create a new instance of the framework.
     *
     * @param batchInserter that the framework should use.
     */
    public BatchGraphAwareFramework(TransactionSimulatingBatchInserter batchInserter) {
        this.batchInserter = batchInserter;
        findRootOrThrowException();
    }

    /**
     * Create a new instance of the framework.
     *
     * @param batchInserter that the framework should use.
     * @param configuration of the framework.
     */
    public BatchGraphAwareFramework(TransactionSimulatingBatchInserter batchInserter, FrameworkConfiguration configuration) {
        super(configuration);
        this.batchInserter = batchInserter;
        findRootOrThrowException();
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
    protected void doInitialize(GraphAwareModule module) {
        module.initialize(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doReinitialize(GraphAwareModule module) {
        module.reinitialize(batchInserter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRecordInitialization(final GraphAwareModule module, final String key) {
        findRootOrThrowException().setProperty(key, CONFIG + module.asString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeUnusedModules(final Collection<String> unusedModules) {
        for (String toRemove : unusedModules) {
            LOG.info("Removing unused module " + toRemove + ".");
            findRootOrThrowException().removeProperty(toRemove);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forceInitialization(final GraphAwareModule module) {
        findRootOrThrowException().setProperty(moduleKey(module), FORCE_INITIALIZATION + System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getRoot() {
        if (!batchInserter.nodeExists(0)) {
            throw new NotFoundException();
        }
        return new BatchInserterNode(0, batchInserter);
    }
}
