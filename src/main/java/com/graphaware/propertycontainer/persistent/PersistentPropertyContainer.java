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

package com.graphaware.propertycontainer.persistent;

import com.graphaware.propertycontainer.dto.plain.property.MutablePropertiesImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

/**
 * {@link Persistent} {@link org.neo4j.graphdb.PropertyContainer}. ID of a detached {@link org.neo4j.graphdb.PropertyContainer} that has never been
 * persisted is -1 by definition.
 * <p/>
 * Experimental, will probably go away / be changed
 */
public abstract class PersistentPropertyContainer<P extends PropertyContainer> implements PropertyContainer, Persistent {

    protected transient volatile P realPropertyContainer = null;
    private transient volatile long id = -1;

    private final MutablePropertiesImpl properties = new MutablePropertiesImpl();

    /**
     * Construct a detached {@link org.neo4j.graphdb.PropertyContainer} that has never been persisted.
     */
    protected PersistentPropertyContainer() {
    }

    /**
     * Construct a detached {@link org.neo4j.graphdb.PropertyContainer}, which has previously been persisted.
     *
     * @param id ID of the container in the database.
     */
    protected PersistentPropertyContainer(long id) {
        setId(id);
    }

    /**
     * Construct a persisted {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param realPropertyContainer the real {@link org.neo4j.graphdb.PropertyContainer} that this represents.
     */
    protected PersistentPropertyContainer(P realPropertyContainer) {
        this.realPropertyContainer = realPropertyContainer;
    }

    /**
     * Get ID of this {@link org.neo4j.graphdb.PropertyContainer} in the database.
     *
     * @return ID in the database, -1 if never persisted.
     */
    public final long getId() {
        if (notPersisted()) {
            return id;
        }
        return doGetId();
    }

    /**
     * Get ID of the underlying real {@link org.neo4j.graphdb.PropertyContainer}. It is guaranteed that the underlying {@link #realPropertyContainer}
     * is not null when this method is called.
     *
     * @return ID of the {@link #realPropertyContainer}.
     */
    protected abstract long doGetId();

    /**
     * Set ID of this {@link org.neo4j.graphdb.PropertyContainer}, thus make it look like it has previously been persisted. This method
     * must be called on a detached object.
     *
     * @param id to set.
     * @throws IllegalStateException if this object is persisted.
     */
    public final void setId(long id) {
        if (persisted()) {
            throw new IllegalStateException("Trying to set ID on a persisted Property Container");
        }

        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if this object is detached.
     */
    @Override
    public GraphDatabaseService getGraphDatabase() {
        persistedOrException();
        return realPropertyContainer.getGraphDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        if (notPersisted()) {
            return properties.containsKey(key);
        }
        return realPropertyContainer.hasProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key) {
        if (notPersisted()) {
            if (!hasProperty(key)) {
                throw new NotFoundException(this + "does not have a property with key " + key);
            }
            return properties.get(key);
        }
        return realPropertyContainer.getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key, Object defaultValue) {
        if (notPersisted()) {
            if (!hasProperty(key)) {
                return defaultValue;
            }
            return properties.get(key);
        }
        return realPropertyContainer.getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        if (notPersisted()) {
            properties.setProperty(key, value);
        } else {
            realPropertyContainer.setProperty(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        if (notPersisted()) {
            Object result = properties.get(key);
            properties.removeProperty(key);
            return result;
        }
        return realPropertyContainer.removeProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        if (notPersisted()) {
            return properties.keySet();
        }
        return realPropertyContainer.getPropertyKeys();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Object> getPropertyValues() {
        if (notPersisted()) {
            return properties.values();
        }
        return realPropertyContainer.getPropertyValues();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void detach() {
        if (notPersisted()) {
            throw new IllegalStateException("Property Container is already detached");
        }

        id = doGetId();

        properties.clear();
        for (String key : realPropertyContainer.getPropertyKeys()) {
            properties.setProperty(key, realPropertyContainer.getProperty(key));
        }

        doDetach();

        realPropertyContainer = null;
    }

    /**
     * Detach itself after properties have been detached.
     */
    protected abstract void doDetach();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void merge(GraphDatabaseService database) {
        doMerge(database);

        //subclass must make sure it sets the real PC
        persistedOrException("Property Container hasn't been properly merged, this is a bug.");

        for (String key : realPropertyContainer.getPropertyKeys()) {
            realPropertyContainer.removeProperty(key);
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            realPropertyContainer.setProperty(entry.getKey(), entry.getValue());
        }

        properties.clear();
    }

    /**
     * Merge itself. When this method returns, the {@link #realPropertyContainer} must not be null. Assumes a running
     * transaction.
     *
     * @param database to merge (attach) to.
     */
    protected abstract void doMerge(GraphDatabaseService database);

    /**
     * @return true iff persisted, i.e. not detached.
     */
    protected final boolean persisted() {
        return !notPersisted();
    }

    /**
     * @return true iff detached, i.e. not persisted.
     */
    protected final boolean notPersisted() {
        return realPropertyContainer == null;
    }

    /**
     * @throws IllegalStateException iff not persisted.
     */
    protected final void persistedOrException() {
        persistedOrException("Property Container hasn't been persisted");
    }

    /**
     * @param message for the exception.
     * @throws IllegalStateException with the given message iff not persisted.
     */
    protected final void persistedOrException(String message) {
        if (notPersisted()) {
            throw new IllegalStateException(message);
        }
    }
}
