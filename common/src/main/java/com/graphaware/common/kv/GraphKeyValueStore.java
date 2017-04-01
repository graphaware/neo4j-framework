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

package com.graphaware.common.kv;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.core.GraphProperties;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

/**
 * {@link com.graphaware.common.kv.KeyValueStore} backed by {@link org.neo4j.graphdb.GraphDatabaseService}.
 */
public class GraphKeyValueStore implements KeyValueStore {

    private final GraphProperties properties;

    public GraphKeyValueStore(GraphDatabaseService database) {
        properties = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(NodeManager.class).newGraphProperties();
    }

    @Override
    public boolean hasKey(String key) {
        return properties.hasProperty(key);
    }

    @Override
    public Object get(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Object get(String key, Object defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public void set(String key, Object value) {
        properties.setProperty(key, value);
    }

    @Override
    public Object remove(String key) {
        return properties.removeProperty(key);
    }

    @Override
    public Iterable<String> getKeys() {
        return properties.getPropertyKeys();
    }
}
