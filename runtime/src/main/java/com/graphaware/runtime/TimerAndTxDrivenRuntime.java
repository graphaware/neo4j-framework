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

import com.graphaware.runtime.manager.*;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.HashSet;
import java.util.Set;


/**
 * {@link GraphAwareRuntime} that operates on a real {@link GraphDatabaseService}.
 */
public class TimerAndTxDrivenRuntime extends DatabaseBackedRuntime {

    private final TimerDrivenModuleManager timerDrivenModuleManager;

    protected TimerAndTxDrivenRuntime(GraphDatabaseService database, TransactionDrivenModuleManager<TxDrivenModule> transactionDrivenModuleManager, TimerDrivenModuleManager timerDrivenModuleManager) {
        super(database, transactionDrivenModuleManager);
        this.timerDrivenModuleManager = timerDrivenModuleManager;
        database.registerTransactionEventHandler(this);
        database.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<String> initializeModules() {
        Set<String> result = new HashSet<>();
        result.addAll(super.initializeModules());
        result.addAll(timerDrivenModuleManager.initializeModules());
        return result;
    }

    @Override
    protected void removeUnusedModules(Set<String> usedModules) {
        super.removeUnusedModules(usedModules);
        timerDrivenModuleManager.performCleanup(usedModules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRegisterModule(RuntimeModule module) {
        super.doRegisterModule(module);

        if (module instanceof TimerDrivenModule) {
            timerDrivenModuleManager.registerModule((TimerDrivenModule) module);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdownModules() {
        super.shutdownModules();

        timerDrivenModuleManager.stopModules();
    }
}
