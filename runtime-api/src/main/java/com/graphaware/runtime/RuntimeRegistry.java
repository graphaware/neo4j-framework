package com.graphaware.runtime;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry of runtimes.
 */
public class RuntimeRegistry {

    private static final Map<GraphDatabaseService, GraphAwareRuntime> RUNTIMES = new HashMap<>();

    /**
     * Register a runtime.
     *
     * @param database against which the runtime is running.
     * @param runtime  the runtime.
     */
    public static void registerRuntime(GraphDatabaseService database, GraphAwareRuntime runtime) {
        RUNTIMES.put(database, runtime);
    }

    /**
     * Get the {@link GraphAwareRuntime} registered with the given database.
     *
     * @param database for which to get runtime.
     * @return the runtime, null if none registered.
     */
    public static GraphAwareRuntime getRuntime(GraphDatabaseService database) {
        return RUNTIMES.get(database);
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
        RUNTIMES.remove(database);
    }
}
