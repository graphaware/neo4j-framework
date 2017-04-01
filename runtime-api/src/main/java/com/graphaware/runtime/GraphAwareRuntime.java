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
import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.neo4j.graphdb.NotFoundException;

/**
 * Runtime that delegates to registered {@link com.graphaware.runtime.module.RuntimeModule}s to perform useful work.
 * There must be exactly one instance of this runtime for a single {@link org.neo4j.graphdb.GraphDatabaseService}.
 * <p>
 * After all desired modules have been registered, {@link #start()} can be called in order to initialize the runtime and
 * all its modules before the database is exposed to callers. No more modules can be registered thereafter.
 * <p>
 * If not called explicitly, the {@link #start()} method shall be called automatically by the runtime upon first
 * transaction received from callers. In such case, all other transaction will be blocked until the runtime and all its
 * modules have been initialized.
 * <p>
 * Every new {@link com.graphaware.runtime.module.RuntimeModule} whose configuration has changed since the last run will
 * be forced to (re-)initialize, which can lead to very long
 * startup times, as (re-)initialization could be a global graph operation. Re-initialization will also be automatically
 * performed for all modules, for which it has been detected that something is out-of-sync
 * (module threw a {@link com.graphaware.runtime.module.NeedsInitializationException}).
 * <p>
 * The runtime might use special nodes for internal data storage and prevent the deletion of those nodes.
 */
public interface GraphAwareRuntime {

    /**
     * Register a {@link com.graphaware.runtime.module.RuntimeModule}. Note that modules are delegated to in the order
     * they are registered. Must be called before the Runtime is started.
     *
     * @param module to register.
     */
    void registerModule(RuntimeModule module);

    /**
     * Start the Runtime. Must be called before anything gets written into the database, but will be called automatically
     * if not called explicitly. Automatic invocation means that first transactions run against the database will have
     * to wait for the Runtime to be started and modules initialized.
     */
    void start();

    /**
     * Blocks until this runtime has been started and is ready to use.
     */
    void waitUntilStarted();

    /**
     * Get a module registered with the runtime.
     *
     * @param moduleId module ID.
     * @param clazz    expected class of the module.
     * @param <T>      type of the class above.
     * @return module.
     * @throws NotFoundException in case no such module has been registered.
     */
    <T extends RuntimeModule> T getModule(String moduleId, Class<T> clazz) throws NotFoundException;

    /**
     * Get a module registered with the runtime.
     *
     * @param clazz class of the module.
     * @param <T>   type of the class above.
     * @return module.
     * @throws NotFoundException     in case no such module has been registered.
     * @throws IllegalStateException in case more than one such module has been registered.
     */
    <T extends RuntimeModule> T getModule(Class<T> clazz) throws NotFoundException;

    /**
     * Get the configuration of this runtime.
     *
     * @return config.
     */
    RuntimeConfiguration getConfiguration();

    /**
     * Get an instance of database writer associated with this runtime. Modules should use this writer to execute database
     * modifications. This allows the framework (and its configuration) to optimize write throughput.
     *
     * @return writer associated with this runtime.
     */
    Neo4jWriter getDatabaseWriter();
}
