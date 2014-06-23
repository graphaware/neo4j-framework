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

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.manager.*;
import com.graphaware.runtime.metadata.ProductionSingleNodeModuleMetadataRepository;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TimerDrivenRuntimeModule;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;


/**
 * {@link GraphAwareRuntime} that operates on a real {@link GraphDatabaseService}.
 */
public class ProductionGraphAwareRuntime extends BaseGraphAwareRuntime implements GraphAwareRuntime {
    private static final Logger LOG = Logger.getLogger(ProductionGraphAwareRuntime.class);

    private final GraphDatabaseService database;

    private final TimerDrivenModuleManager timerDrivenModuleManager;

    /**
     * Create a new instance of the runtime.
     *
     * @param database on which the runtime should operate.
     */
    public ProductionGraphAwareRuntime(GraphDatabaseService database) {
        super(new ProductionTransactionDrivenModuleManager(new ProductionSingleNodeModuleMetadataRepository(database, DefaultRuntimeConfiguration.getInstance()), database));
        this.database = database;
        this.timerDrivenModuleManager = new TimerDrivenModuleManagerImpl(new ProductionSingleNodeModuleMetadataRepository(database, DefaultRuntimeConfiguration.getInstance()), database);
        registerSelfAsHandler();
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



    @Override
    protected void initializeModules() {
        super.initializeModules();
        timerDrivenModuleManager.initializeModules();
    }

    @Override
    protected void doRegisterModule(RuntimeModule module) {
        super.doRegisterModule(module);

        if (module instanceof TimerDrivenRuntimeModule) {
            timerDrivenModuleManager.registerModule((TimerDrivenRuntimeModule) module);
        }
    }

    @Override
    protected void doShutdown() {
        super.doShutdown();

        timerDrivenModuleManager.shutdownModules();
    }
}
