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

package com.graphaware.runtime.manager;

import com.graphaware.runtime.module.RuntimeModule;

import java.util.Set;

/**
 * A manager of {@link RuntimeModule}s, which takes care of their lifecycle.
 *
 * @param <T> type of module this manager can manage.
 */
public interface ModuleManager<T extends RuntimeModule> {

    /**
     * Check that the given module isn't already registered with the manager.
     *
     * @param module to check.
     * @throws IllegalStateException in case the module is already registered.
     */
    void checkNotAlreadyRegistered(RuntimeModule module);

    /**
     * Register a module with this manager.
     *
     * @param module to register.
     * @throws IllegalStateException in case the module is already registered.
     */
    void registerModule(T module);

    /**
     * Get a module registered with the manager.
     *
     * @param moduleId module ID.
     * @param clazz    expected class of the module.
     * @param <M>      type of the class above.
     * @return module, <code>null</code> if no such module exists.
     */
    <M extends RuntimeModule> M getModule(String moduleId, Class<M> clazz);

    /**
     * Get a module registered with the manager.
     *
     * @param clazz class of the module.
     * @param <M>   type of the class above.
     * @return module. <code>null</code> if no such module exists.
     * @throws IllegalStateException if more than one module of the same type has been registered.
     */
    <M extends RuntimeModule> M getModule(Class<M> clazz);

    /**
     * Load module metadata from wherever they are stored in between database restarts and do whatever is necessary
     * to do with this metadata before the modules can be used.
     *
     * @return IDs of all modules registered with this manager.
     */
    Set<String> loadMetadata();

    /**
     * Perform cleanup metadata written by modules that are no longer present.
     *
     * @param usedModules IDs of all modules that are known to be used by the runtime during the current run.
     */
    void cleanupMetadata(Set<String> usedModules);

    /**
     * Perform work needed to make modules start doing their job. Called exactly once each time the database is started.
     */
    void startModules();

    /**
     * Bring all modules to an orderly shutdown, when the database is stopped.
     */
    void shutdownModules();
}
