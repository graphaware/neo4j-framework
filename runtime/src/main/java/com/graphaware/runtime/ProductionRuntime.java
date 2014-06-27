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
 * {@link DatabaseBackedRuntime} intended for production use.
 * <p/>
 * Supports both {@link TimerDrivenModule} and {@link TxDrivenModule} {@link RuntimeModule}s.
 * <p/>
 * To use this {@link GraphAwareRuntime}, please construct it using {@link GraphAwareRuntimeFactory}.
 */
public class ProductionRuntime extends DatabaseBackedRuntime {

    private final TimerDrivenModuleManager timerDrivenModuleManager;

    /**
     * Construct a new runtime.
     *
     * @param database                 on which the runtime operates.
     * @param txDrivenModuleManager    manager for transaction-driven modules.
     * @param timerDrivenModuleManager manager for timer-driven modules.
     */
    protected ProductionRuntime(GraphDatabaseService database, TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager, TimerDrivenModuleManager timerDrivenModuleManager) {
        super(database, txDrivenModuleManager);
        this.timerDrivenModuleManager = timerDrivenModuleManager;
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
    protected void doStart(boolean skipInitialization) {
        super.doStart(skipInitialization);
        timerDrivenModuleManager.startModules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCleanup(Set<String> usedModules) {
        super.performCleanup(usedModules);
        timerDrivenModuleManager.removeUnusedModules(usedModules);
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

        timerDrivenModuleManager.shutdownModules();
    }
}
