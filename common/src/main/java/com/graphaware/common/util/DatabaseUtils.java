package com.graphaware.common.util;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Utilities for dealing with the Neo4j database.
 */
public final class DatabaseUtils {

    private static Set<GraphDatabaseService> databases = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<GraphDatabaseService, Boolean>()));

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (GraphDatabaseService database : databases) {
                    database.shutdown();
                }
            }
        });
    }

    /**
     * Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits
     * (even if you "Ctrl-C" the running application).
     */
    public static void registerShutdownHook(final GraphDatabaseService database) {
        databases.add(database);
    }

    private DatabaseUtils() {
    }
}
