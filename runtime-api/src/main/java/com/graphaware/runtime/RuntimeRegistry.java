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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry of runtimes.
 */
public class RuntimeRegistry {

    private static final Map<String, GraphAwareRuntime> RUNTIMES = new HashMap<>();

    /**
     * Register a runtime.
     *
     * @param database against which the runtime is running.
     * @param runtime  the runtime.
     */
    public static void registerRuntime(GraphDatabaseService database, GraphAwareRuntime runtime) {
        RUNTIMES.put(storeDir(database), runtime);
    }

    /**
     * Get the {@link GraphAwareRuntime} registered with the given database.
     *
     * @param database for which to get runtime.
     * @return the runtime, null if none registered.
     */
    public static GraphAwareRuntime getRuntime(GraphDatabaseService database) {
        return RUNTIMES.get(storeDir(database));
    }

    /**
     * Get the {@link GraphAwareRuntime} registered with the given database.
     *
     * @param database for which to get runtime.
     * @return the runtime, which is guaranteed to be started upon return.
     * @throws IllegalStateException in case no runtime is registered with this database.
     */
    public static GraphAwareRuntime getStartedRuntime(GraphDatabaseService database) {
        GraphAwareRuntime runtime = getRuntime(database);
        if (runtime == null) {
            throw new IllegalStateException("No GraphAware Runtime is registered with the given database");
        }
        runtime.waitUntilStarted();
        return runtime;
    }

    /**
     * Remove a runtime from the registry.
     *
     * @param database against which the runtime to be removed is running.
     */
    public static void removeRuntime(GraphDatabaseService database) {
        RUNTIMES.remove(storeDir(database));
    }

    public static void clear() {
        RUNTIMES.clear();
    }

    private static String storeDir(GraphDatabaseService database) {
        return ((GraphDatabaseAPI) database).getStoreDir();
    }
}
