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

import com.graphaware.runtime.manager.TimerDrivenModuleManager;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * {@link DatabaseRuntime} intended for production use.
 * <p/>
 * Supports both {@link TimerDrivenModule} and {@link TxDrivenModule} {@link RuntimeModule}s.
 * <p/>
 * To use this {@link GraphAwareRuntime}, please construct it using {@link GraphAwareRuntimeFactory}.
 */
public class ProductionRuntime extends DatabaseRuntime {

    private static final Map<GraphDatabaseService, ProductionRuntime> RUNTIMES = new HashMap<>();

    private final TimerDrivenModuleManager timerDrivenModuleManager;

    /**
     * Construct a new runtime. Protected, please use {@link GraphAwareRuntimeFactory}.
     *
     * @param database                 on which the runtime operates.
     * @param txDrivenModuleManager    manager for transaction-driven modules.
     * @param timerDrivenModuleManager manager for timer-driven modules.
     */
    protected ProductionRuntime(GraphDatabaseService database, TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager, TimerDrivenModuleManager timerDrivenModuleManager) {
        super(database, txDrivenModuleManager);
        this.timerDrivenModuleManager = timerDrivenModuleManager;

        if (getRuntime(database) != null) {
            throw new IllegalStateException("It is not possible to create multiple runtimes for a single database!");
        }

        RUNTIMES.put(database, this);
    }

    /**
     * Get the {@link ProductionRuntime} registered with the given database.
     *
     * @param database for which to get runtime.
     * @return the runtime, null if none registered.
     */
    public static ProductionRuntime getRuntime(GraphDatabaseService database) {
        return RUNTIMES.get(database);
    }

    /**
     * Get the {@link ProductionRuntime} registered with the given database.
     *
     * @param database for which to get runtime.
     * @return the runtime, which is guaranteed to be started upon return.
     * @throws IllegalStateException in case no runtime is registered with this database.
     */
    public static ProductionRuntime getStartedRuntime(GraphDatabaseService database) {
        ProductionRuntime runtime = getRuntime(database);
        if (runtime == null) {
            throw new IllegalStateException("No GraphAware Runtime is registered with the given database");
        }
        runtime.waitUntilStarted();
        return runtime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(boolean skipLoadingMetadata) {
        super.doStart(skipLoadingMetadata);
        timerDrivenModuleManager.startModules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<String> loadMetadata() {
        Set<String> result = new HashSet<>();
        result.addAll(super.loadMetadata());
        result.addAll(timerDrivenModuleManager.loadMetadata());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanupMetadata(Set<String> usedModules) {
        super.cleanupMetadata(usedModules);
        timerDrivenModuleManager.cleanupMetadata(usedModules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkNotAlreadyRegistered(RuntimeModule module) {
        super.checkNotAlreadyRegistered(module);
        timerDrivenModuleManager.checkNotAlreadyRegistered(module);
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
    public <M extends RuntimeModule> M getModule(String moduleId, Class<M> clazz) throws NotFoundException {
        try {
            return super.getModule(moduleId, clazz);
        } catch (NotFoundException e) {
            M module = timerDrivenModuleManager.getModule(moduleId, clazz);

            if (module == null) {
                throw new NotFoundException("No module of type " + clazz.getName() + " with ID " + moduleId + " has been registered");
            }

            return module;
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterShutdown(GraphDatabaseService database) {
        super.afterShutdown(database);
        RUNTIMES.remove(database);
    }
}
