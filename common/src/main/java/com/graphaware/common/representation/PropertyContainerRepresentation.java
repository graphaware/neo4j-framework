/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.common.representation;

import com.graphaware.common.util.PropertyContainerUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Serializable} representation of a Neo4j property container.
 *
 * @param <T> type of the {@link PropertyContainer} this class represents.
 */
public abstract class PropertyContainerRepresentation<T extends PropertyContainer> implements Serializable {
    static final long NEW = -1;

    private long id = NEW;
    private Map<String, Object> properties;

    /**
     * No-arg constructor (for Jackson et al).
     */
    protected PropertyContainerRepresentation() {
    }

    /**
     * Construct a new representation from a property container.
     *
     * @param pc         to construct a representation from. Must not be <code>null</code>.
     * @param properties keys of properties to be included in the representation.
     *                   Can be <code>null</code>, which represents all. Empty array represents none.
     */
    protected PropertyContainerRepresentation(T pc, String[] properties) {
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
    protected PropertyContainerRepresentation(long id) {
        this.id = id;
    }

    /**
     * Construct a new representation of a property container from a map of properties.
     *
     * @param properties of the new container. Can be <code>null</code>, which is equivalent to an empty map.
     */
    protected PropertyContainerRepresentation(Map<String, Object> properties) {
        setProperties(properties);
    }

    /**
     * Construct a new representation of a property container from its internal Neo4j ID and a map of properties.
     * <p/>
     * Note that this constructor is only intended for testing.
     *
     * @param id         ID.
     * @param properties of the new container. Can be <code>null</code>, which is equivalent to an empty map.
     */
    protected PropertyContainerRepresentation(long id, Map<String, Object> properties) {
        this.id = id;
        setProperties(properties);
    }

    /**
     * Add a property.
     *
     * @param key   key. Must not be <code>null</code>.
     * @param value value. Must not be <code>null</code>.
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

    /**
     * @return properties. Can be <code>null</code>.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Set properties.
     *
     * @param properties Can be <code>null</code>, which is equivalent to an empty map.
     */
    public void setProperties(Map<String, Object> properties) {
        if (properties == null) {
            this.properties = null;
        } else {
            initPropsIfNeeded();
            this.properties.putAll(properties);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PropertyContainerRepresentation<?> that = (PropertyContainerRepresentation<?>) o;

        if (id != that.id) {
            return false;
        }
        return !(properties != null ? !properties.equals(that.properties) : that.properties != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}
