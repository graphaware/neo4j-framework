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

import com.graphaware.framework.config.DefaultFrameworkConfiguration;
import com.graphaware.framework.config.FrameworkConfiguration;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Collection;


/**
 * {@link BaseGraphAwareFramework} that operates on a real {@link GraphDatabaseService}.
 *
 * @see BaseGraphAwareFramework
 */
public class GraphAwareFramework extends BaseGraphAwareFramework {
    private static final Logger LOG = Logger.getLogger(GraphAwareFramework.class);

    private final GraphDatabaseService database;

    /**
     * Create a new instance of the framework.
     *
     * @param database on which the framework should operate.
     */
    public GraphAwareFramework(GraphDatabaseService database) {
        super(DefaultFrameworkConfiguration.getInstance());
        this.database = database;
        findRootOrThrowException();  //fail fast
    }

    /**
     * Create a new instance of the framework.
     *
     * @param database      on which the framework should operate.
     * @param configuration of the framework.
     */
    public GraphAwareFramework(GraphDatabaseService database, FrameworkConfiguration configuration) {
        super(configuration);
        this.database = database;
        findRootOrThrowException();  //fail fast
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSelfAsHandler() {
        database.registerTransactionEventHandler(this);
        database.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInitialize(GraphAwareModule module) {
        module.initialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doReinitialize(GraphAwareModule module) {
        module.reinitialize(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRecordInitialization(final GraphAwareModule module, final String key) {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                findRootOrThrowException().setProperty(key, CONFIG + module.asString());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeUnusedModules(final Collection<String> unusedModules) {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                for (String toRemove : unusedModules) {
                    LOG.info("Removing unused module " + toRemove + ".");
                    findRootOrThrowException().removeProperty(toRemove);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void forceInitialization(final GraphAwareModule module) {
        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                findRootOrThrowException().setProperty(moduleKey(module), FORCE_INITIALIZATION + System.currentTimeMillis());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getRoot() {
        return database.getNodeById(0);
    }
}
