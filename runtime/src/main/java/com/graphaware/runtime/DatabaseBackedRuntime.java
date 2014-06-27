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

import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;


/**
 * {@link TxDrivenRuntime} that operates on a real {@link org.neo4j.graphdb.GraphDatabaseService}.
 */
public class DatabaseBackedRuntime extends TxDrivenRuntime<TxDrivenModule> {

    private final GraphDatabaseService database;

    /**
     * Construct a new runtime.
     *
     * @param database              on which the runtime operates.
     * @param txDrivenModuleManager manager for transaction-driven modules.
     */
    protected DatabaseBackedRuntime(GraphDatabaseService database, TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager) {
        super(txDrivenModuleManager);
        this.database = database;
        database.registerTransactionEventHandler(this);
        database.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<TxDrivenModule> supportedModule() {
        return TxDrivenModule.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean databaseAvailable() {
        return database.isAvailable(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Transaction startTransaction() {
        return database.beginTx();
    }
}
