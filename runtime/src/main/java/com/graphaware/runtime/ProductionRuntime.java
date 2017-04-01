/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.manager.TimerDrivenModuleManager;
import com.graphaware.runtime.manager.TxDrivenModuleManager;
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.module.TxDrivenModule;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;

import java.util.HashSet;
import java.util.Set;


/**
 * {@link DatabaseRuntime} intended for production use.
 * <p>
 * Supports both {@link TimerDrivenModule} and {@link TxDrivenModule} {@link RuntimeModule}s.
 * <p>
 * To use this {@link GraphAwareRuntime}, please construct it using {@link GraphAwareRuntimeFactory}.
 */
public class ProductionRuntime extends DatabaseRuntime {

    private final TimerDrivenModuleManager timerDrivenModuleManager;

    /**
     * Construct a new runtime. Protected, please use {@link GraphAwareRuntimeFactory}.
     *
     * @param configuration            config.
     * @param database                 on which the runtime operates.
     * @param txDrivenModuleManager    manager for transaction-driven modules.
     * @param timerDrivenModuleManager manager for timer-driven modules.
     * @param writer                   to use when writing to the database.
     */
    protected ProductionRuntime(RuntimeConfiguration configuration, GraphDatabaseService database, TxDrivenModuleManager<TxDrivenModule> txDrivenModuleManager, TimerDrivenModuleManager timerDrivenModuleManager, Neo4jWriter writer) {
        super(configuration, database, txDrivenModuleManager, writer);
        this.timerDrivenModuleManager = timerDrivenModuleManager;

        if (RuntimeRegistry.getRuntime(database) != null) {
            throw new IllegalStateException("It is not possible to create multiple runtimes for a single database!");
        }

        RuntimeRegistry.registerRuntime(database, this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void startModules() {
        super.startModules();
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
    public <M extends RuntimeModule> M getModule(Class<M> clazz) throws NotFoundException {
        M result = null;
        try {
            result = super.getModule(clazz);
        } catch (NotFoundException e) {
            //ok
        }

        try {
            M potentialResult = timerDrivenModuleManager.getModule(clazz);
            if (potentialResult != null) {
                if (result != null && potentialResult != result) {
                    throw new IllegalStateException("More than one module of type " + clazz + " has been registered");
                }
                result = potentialResult;
            }
        } catch (NotFoundException e) {
            //ok
        }

        if (result == null) {
            throw new NotFoundException("No module of type " + clazz.getName() + " has been registered");
        }

        return result;
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
        RuntimeRegistry.removeRuntime(database);
    }
}
