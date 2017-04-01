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
