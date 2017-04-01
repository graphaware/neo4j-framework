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

/**
 * A key-value store.
 */
public interface KeyValueStore {

    /**
     * Does the store contain a value for the given key?
     *
     * @param key to check. Must not be <code>null</code>.
     * @return <code>true</code> iff there's a value associated with the key.
     */
    boolean hasKey(String key);

    /**
     * Get the value associated with the given key.
     *
     * @param key key. Must not be <code>null</code>.
     * @return the value associated with the given key.
     * @throws org.neo4j.graphdb.NotFoundException if there's no property associated with the key.
     */
    Object get(String key);

    /**
     * Get the value associated with the given key, or a default value.
     *
     * @param key          key. Must not be <code>null</code>.
     * @param defaultValue the default value that will be returned if no value was associated with the given key
     * @return the value associated with the given key.
     */
    Object get(String key, Object defaultValue);

    /**
     * Set the value for the given key.
     *
     * @param key   the key with which the new value will be associated. Must not be <code>null</code>.
     * @param value the new value. Must not be <code>null</code>.
     * @throws IllegalArgumentException if <code>value</code> is of an unsupported type (including <code>null</code>).
     */
    void set(String key, Object value);

    /**
     * Remove the value associated with the given key and return the old value. If there's no value associated with the key, <code>null</code>
     * will be returned.
     *
     * @param key the property key. Must not be <code>null</code>.
     * @return the value that used to be associated with the given key.
     */
    Object remove(String key);

    /**
     * Returns all existing keys, or an empty iterable if this store is empty.
     *
     * @return all keys stored.
     */
    Iterable<String> getKeys();
}
