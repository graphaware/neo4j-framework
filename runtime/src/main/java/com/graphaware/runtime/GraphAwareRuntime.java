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

package com.graphaware.runtime;

import com.graphaware.runtime.module.Module;
import org.neo4j.graphdb.NotFoundException;

/**
 * Runtime that delegates to registered {@link Module}s to perform useful work.
 * There must be exactly one instance of this runtime for a single {@link org.neo4j.graphdb.GraphDatabaseService}.
 * <p>
 * After all desired modules have been registered, {@link #start()} can be called in order to initialize the runtime and
 * all its modules before the database is exposed to callers. No more modules can be registered thereafter.
 * <p>
 * If not called explicitly, the {@link #start()} method shall be called automatically by the runtime upon first
 * transaction received from callers. In such case, all other transaction will be blocked until the runtime and all its
 * modules have been initialized.
 */
public interface GraphAwareRuntime {

    /**
     * Prefix for GraphAware internal nodes, relationships, and properties. This is fixed as there is little chance
     * that users would have a reason to change it.
     */
    String GA_PREFIX = "_GA_";

    /**
     * Register a {@link Module}. Note that modules are delegated to in the order
     * they are registered. Must be called before the Runtime is started.
     *
     * @param module to register.
     */
    void registerModule(Module<?> module);

    /**
     * Start the Runtime. Must be called before anything gets written into the database, but will be called automatically
     * if not called explicitly. Automatic invocation means that first transactions run against the database will have
     * to wait for the Runtime to be started and modules initialized.
     */
    void start();

    /**
     * Stop the Runtime. All modules will be stopped first, before this method call returns. Should be called before
     * the database is stopped, but the Framework will do this automatically.
     */
    void stop();

    /**
     * Get a module registered with the runtime.
     *
     * @param moduleId module ID.
     * @param clazz    expected class of the module.
     * @param <T>      type of the class above.
     * @return module.
     * @throws NotFoundException in case no such module has been registered.
     */
    <T extends Module<?>> T getModule(String moduleId, Class<T> clazz) throws NotFoundException;

    /**
     * Get a module registered with the runtime.
     *
     * @param clazz class of the module.
     * @param <T>   type of the class above.
     * @return module.
     * @throws NotFoundException     in case no such module has been registered.
     * @throws IllegalStateException in case more than one such module has been registered.
     */
    <T extends Module<?>> T getModule(Class<T> clazz) throws NotFoundException;
}
