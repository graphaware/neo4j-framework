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

/**
 * A mutable representation of Neo4j properties, usually coming from a {@link org.neo4j.graphdb.PropertyContainer}.
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
public interface MutableProperties<V> extends ImmutableProperties<V> {

    /**
     * Set a property.
     *
     * @param key   key, must not be null or empty.
     * @param value value, can be null and empty.
     */
    void setProperty(String key, V value);

    /**
     * Remove a property.
     *
     * @param key key of the property to be removed, must not be null or empty.
     * @return true iff the property has been removed (was present).
     */
    boolean removeProperty(String key);
}
