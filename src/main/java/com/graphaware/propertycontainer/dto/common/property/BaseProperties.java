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

package com.graphaware.propertycontainer.dto.common.property;


import org.neo4j.graphdb.PropertyContainer;

import java.util.*;

import static com.graphaware.propertycontainer.util.ArrayUtils.arrayFriendlyMapEquals;
import static com.graphaware.propertycontainer.util.PropertyContainerUtils.cleanKey;

/**
 * Base-class for {@link ImmutableProperties} and {@link MutableProperties} implementations.
 *
 * @param <V> type with which property values are represented.
 */
public abstract class BaseProperties<V> {

    private final Map<String, V> properties;

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    protected BaseProperties(PropertyContainer propertyContainer) {
        properties = newMap();
        properties.putAll(propertiesToMap(propertyContainer));
    }

    /**
     * Convert properties from a {@link org.neo4j.graphdb.PropertyContainer} to a {@link java.util.Map} keyed by the {@link String} property key
     * with the value converted to the right type for the concrete class' purposes.
     *
     * @param propertyContainer to take properties from.
     * @return converted properties as {@link java.util.Map}.
     */
    protected abstract Map<String, V> propertiesToMap(PropertyContainer propertyContainer);

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    protected BaseProperties(Map<String, ?> properties) {
        this.properties = newMap();
        this.properties.putAll(cleanProperties(properties));
    }

    /**
     * Clean properties, i.e. remove/fix disallowed values, throw exceptions for empty keys, etc.
     *
     * @param properties to clean.
     * @return cleaned properties.
     */
    protected abstract Map<String, V> cleanProperties(Map<String, ?> properties);

    /**
     * Construct a new {@link Map} where properties will be stored.
     *
     * @return a map.
     */
    protected Map<String, V> newMap() {
        return new HashMap<>();
    }

    /**
     * Set a property.
     *
     * @param key   key, must not be null or empty.
     * @param value value, can be null and empty, null will be converted to empty String.
     */
    protected void setProperty(String key, V value) {
        properties.put(cleanKey(key), value);
    }

    /**
     * Remove a property.
     *
     * @param key key of the property to be removed, must not be null or empty.
     * @return true iff the property has been removed (was present).
     */
    protected boolean removeProperty(String key) {
        return properties.remove(key) != null;
    }

    /**
     * Clear properties.
     */
    protected void clear() {
        properties.clear();
    }

    /**
     * Does the instance contain (or can it generate) a property with the given key?
     *
     * @param key to check for.
     * @return true iff it contains the key, or if can generate one.
     */
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    /**
     * Get or generate a property with the given key.
     *
     * @param key key.
     * @return value of the property.
     */
    public V get(String key) {
        return properties.get(key);
    }

    /**
     * Get all the stored keys.
     *
     * @return all keys.
     */
    public Set<String> keySet() {
        return properties.keySet();
    }

    /**
     * Get all stored key-value pairs.
     *
     * @return all entries (key-value pairs).
     */
    public Set<Map.Entry<String, V>> entrySet() {
        return properties.entrySet();
    }

    /**
     * Get all the stored values.
     *
     * @return all values.
     */
    public Collection<V> values() {
        return properties.values();
    }

    /**
     * @return true iff there are no properties.
     */
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    /**
     * @return the number of stored properties.
     */
    public int size() {
        return properties.size();
    }

    /**
     * Get an immutable {@link java.util.Map} of represented properties.
     *
     * @return read-only {@link java.util.Map} of properties.
     */
    public Map<String, V> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Do these properties match (are they the same as) the properties held by the given {@link ImmutableProperties}?
     *
     * @param properties to check.
     * @return true iff these properties match the properties held by the given {@link ImmutableProperties}.
     */
    public boolean matches(ImmutableProperties<V> properties) {
        return arrayFriendlyMapEquals(getProperties(), properties.getProperties());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseProperties that = (BaseProperties) o;

        //noinspection RedundantIfStatement
        if (!properties.equals(that.properties)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return properties.hashCode();
    }
}
