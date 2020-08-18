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
import com.graphaware.common.ping.StatsCollector;
import com.graphaware.runtime.module.RuntimeModule;
import org.neo4j.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base-class for {@link ModuleManager} implementations.
 */
public abstract class BaseModuleManager<T extends RuntimeModule> implements ModuleManager<T> {

    private static final Log LOG = LoggerFactory.getLogger(BaseModuleManager.class);

    protected final Map<String, T> modules = new LinkedHashMap<>();
    private final StatsCollector statsCollector;

    /**
     * Construct a new manager.
     */
    protected BaseModuleManager(StatsCollector statsCollector) {
        this.statsCollector = statsCollector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void registerModule(T module) {
        modules.put(module.getId(), module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends RuntimeModule> M getModule(String moduleId, Class<M> clazz) {
        if (!modules.containsKey(moduleId)) {
            return null;
        }

        T module = modules.get(moduleId);
        if (!clazz.isAssignableFrom(module.getClass())) {
            LOG.warn("Module " + moduleId + " is not a " + clazz.getName());
            return null;
        }

        return (M) module;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends RuntimeModule> M getModule(Class<M> clazz) {
        M result = null;
        for (T module : modules.values()) {
            if (clazz.isAssignableFrom(module.getClass())) {
                if (result != null) {
                    throw new IllegalStateException("More than one module of type " + clazz + " has been registered");
                }
                result = (M) module;
            }
        }

        return result;
    }

    /**
     * Check that the given module isn't already registered with the manager.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    public void checkNotAlreadyRegistered(RuntimeModule module) {
        if (modules.values().contains(module)) {
            LOG.error("Module " + module.getId() + " cannot be registered more than once!");
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }

        if (modules.containsKey(module.getId())) {
            LOG.error("Module " + module.getId() + " cannot be registered more than once!");
            throw new IllegalStateException("Module " + module.getId() + " cannot be registered more than once!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startModules() {
        for (T module : modules.values()) {
            statsCollector.moduleStart(module.getClass().getCanonicalName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownModules() {
        for (T module : modules.values()) {
            LOG.info("Shutting down module " + module.getId());
            module.shutdown();
        }
    }
}
