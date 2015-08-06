/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.graphaware.common.util.PropertyContainerUtils;
import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON-serializable representation of a Neo4j property container.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public abstract class JsonPropertyContainer<T extends PropertyContainer> {
    static final long NEW = -1;

    private long id = NEW;
    private Map<String, Object> properties;

    /**
     * No-arg constructor for Jackson.
     */
    protected JsonPropertyContainer() {
    }

    /**
     * Construct a new representation from a property container.
     *
     * @param pc         to construct a representation from.
     * @param properties keys of properties to be included in the representation.
     *                   Can be <code>null</code>, which represents all. Empty array represents none.
     */
    protected JsonPropertyContainer(T pc, String[] properties) {
        this(PropertyContainerUtils.id(pc));

        if (properties != null) {
            for (String property : properties) {
                if (pc.hasProperty(property)) {
                    putProperty(property, pc.getProperty(property));
                }
            }
        } else {
            for (String property : pc.getPropertyKeys()) {
                putProperty(property, pc.getProperty(property));
            }
        }
    }

    /**
     * Construct a representation of a property container from its internal Neo4j ID.
     *
     * @param id ID.
     */
    protected JsonPropertyContainer(long id) {
        this.id = id;
    }

    /**
     * Construct a new representation of a property container from a map of properties.
     *
     * @param properties of the new container.
     */
    protected JsonPropertyContainer(Map<String, Object> properties) {
        setProperties(properties);
    }

    /**
     * Add a property.
     *
     * @param key   key
     * @param value value.
     */
    public void putProperty(String key, Object value) {
        initPropsIfNeeded();
        properties.put(key, value);
    }

    private void initPropsIfNeeded() {
        if (properties == null) {
            properties = new HashMap<>();
        }
    }

    /**
     * Produce a {@link PropertyContainer} from this representation. This means either fetch the container from the
     * given database (iff id is set), or create it.
     *
     * @param database to create/fetch container in.
     * @return container.
     */
    public final T producePropertyContainer(GraphDatabaseService database) {
        T result;

        try (Transaction tx = database.beginTx()) {

            if (getId() == NEW) {
                checkCanCreate();
                result = create(database);
                populate(result);
            } else {
                checkCanFetch();
                result = fetch(database);
            }

            tx.success();
        }

        return result;
    }

    /**
     * Create a new property container from this representation in the given database.
     *
     * @param database to create the container in.
     * @return container.
     */
    protected abstract T create(GraphDatabaseService database);

    /**
     * Fetch a property container from the given database.
     *
     * @param database to fetch in.
     * @return container.
     */
    protected abstract T fetch(GraphDatabaseService database);

    /**
     * Populate this instance of a container representation with data from the given {@link PropertyContainer}.
     *
     * @param t to populate from.
     */
    protected void populate(T t) {
        setId(PropertyContainerUtils.id(t));
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                t.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Check that this instance of property container representation can be created in the db.
     *
     * @throws IllegalStateException if not possible to create.
     */
    protected void checkCanCreate() {

    }

    /**
     * Check that this instance of property container representation can be fetched from the db.
     *
     * @throws IllegalStateException if not possible to fetch.
     */
    protected void checkCanFetch() {
        if (getProperties() != null && !getProperties().isEmpty()) {
            throw new IllegalStateException("Must not specify properties for existing node!");
        }
    }

    //getters & setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        initPropsIfNeeded();
        this.properties.putAll(properties);
    }
}
