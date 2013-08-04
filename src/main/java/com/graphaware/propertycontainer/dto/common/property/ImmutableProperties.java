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

import java.util.Map;
import java.util.Set;

/**
 * An immutable representation of Neo4j properties, usually coming from a {@link org.neo4j.graphdb.PropertyContainer}.
 * <p/>
 * Keys must not be null or empty, values can be any object of type V, or null.
 * <p/>
 * Please note that the semantics of {@link #get(String)} and {@link #containsKey(String)} are slightly different than
 * in a regular {@link java.util.Map} in the sense that implementations can choose to pretend some key-value pairs exist
 * and generate them. In such cases, all the other methods should still work as expected, i.e. consult the underlying
 * properties.
 *
 * @param <V> type with which property values are represented.
 */
public interface ImmutableProperties<V> {

    /**
     * Does the instance contain (or can it generate) a property with the given key?
     *
     * @param key to check for.
     * @return true iff it contains the key, or if can generate one.
     */
    boolean containsKey(String key);

    /**
     * Get or generate a property with the given key.
     *
     * @param key key.
     * @return value of the property.
     */
    V get(String key);

    /**
     * Get all the stored keys.
     *
     * @return all keys.
     */
    Set<String> keySet();

    /**
     * Get all stored key-value pairs.
     *
     * @return all entries (key-value pairs).
     */
    Set<Map.Entry<String, V>> entrySet();

    /**
     * @return true iff there are no properties.
     */
    boolean isEmpty();

    /**
     * @return the number of stored properties.
     */
    int size();

    /**
     * Get an immutable {@link java.util.Map} of represented properties.
     *
     * @return read-only {@link java.util.Map} of properties.
     */
    Map<String, V> getProperties();

    /**
     * Do these properties match (are they the same as) the properties held by the given {@link org.neo4j.graphdb.PropertyContainer}?
     *
     * @param propertyContainer to check.
     * @return true iff the properties held by this instance match the properties held by the given
     *         {@link org.neo4j.graphdb.PropertyContainer}.
     */
    boolean matches(PropertyContainer propertyContainer);

    /**
     * Do these properties match (are they the same as) the properties held by the given {@link ImmutableProperties}?
     *
     * @param properties to check.
     * @return true iff these properties match the properties held by the given {@link ImmutableProperties}.
     */
    boolean matches(ImmutableProperties<V> properties);
}
