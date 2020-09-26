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
import com.graphaware.runtime.module.Module;
import org.neo4j.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base-class for {@link ModuleManager} implementations.
 */
public abstract class BaseModuleManager implements ModuleManager {

    private static final Log LOG = LoggerFactory.getLogger(BaseModuleManager.class);

    protected final Map<String, Module> modules = new LinkedHashMap<>();

    /**
     * Construct a new manager.
     */
    protected BaseModuleManager() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void registerModule(Module module) {
        modules.put(module.getId(), module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends Module<?>> M getModule(String moduleId, Class<M> clazz) {
        if (!modules.containsKey(moduleId)) {
            return null;
        }

        Module module = modules.get(moduleId);
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
    public <M extends Module<?>> M getModule(Class<M> clazz) {
        M result = null;
        for (Module module : modules.values()) {
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
    public void checkNotAlreadyRegistered(Module module) {
        if (modules.containsValue(module)) {
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopModules() {
        for (Module<?> module : modules.values()) {
            LOG.info("Shutting down module " + module.getId());
            module.shutdown();
        }
    }
}
