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

package com.graphaware.propertycontainer.dto.string.property;

import org.neo4j.graphdb.PropertyContainer;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.propertycontainer.util.PropertyContainerUtils.valueToString;

/**
 * An abstract base-class for Neo4j properties representations that wish to implement {@link CopyMakingSerializableProperties}.
 *
 * @param <T> type of object returned with and without a property.
 */
public abstract class BaseCopyMakingSerializableProperties<T extends CopyMakingSerializableProperties<T>> extends SerializablePropertiesImpl {

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    protected BaseCopyMakingSerializableProperties(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    protected BaseCopyMakingSerializableProperties(Map<String, ?> properties) {
        super(properties);
    }

    /**
     * Construct a representation of properties from a {@link String}.
     *
     * @param string    to construct properties from. Must be of the form key1#value1#key2#value2... (assuming # separator).
     * @param separator of keys and values, ideally a single character, must not be null or empty.
     */
    protected BaseCopyMakingSerializableProperties(String string, String separator) {
        super(string, separator);
    }

    /**
     * Construct a representation of properties from a {@link String}.
     *
     * @param string    to construct properties from. Must be of the form prefix + key1#value1#key2#value2... (assuming # separator).
     * @param prefix    of the string that should be removed before conversion.
     * @param separator of keys and values, ideally a single character, must not be null or empty.
     */
    protected BaseCopyMakingSerializableProperties(String string, String prefix, String separator) {
        super(string, prefix, separator);
    }

    /**
     * Make a copy of this instance with a single key-value pair missing.
     *
     * @param key to omit in the copy. If the key does not exist in the representation, an identical copy will be returned.
     * @return copy of this instance except one key-value pair.
     */
    public T without(String key) {
        Map<String, String> newProps = new HashMap<>(getProperties());
        newProps.remove(key);
        return newInstance(newProps);
    }

    /**
     * Make a copy of this instance with an extra key-value pair.
     *
     * @param key   new key
     * @param value new value
     * @return copy of this instance with an extra key-value pair.
     */
    public T with(String key, Object value) {
        Map<String, String> newProps = new HashMap<>(getProperties());
        newProps.put(key, valueToString(value));
        return newInstance(newProps);
    }

    /**
     * Create new instance from a map of properties.
     *
     * @param props props.
     * @return new instance.
     */
    protected abstract T newInstance(Map<String, String> props);
}
