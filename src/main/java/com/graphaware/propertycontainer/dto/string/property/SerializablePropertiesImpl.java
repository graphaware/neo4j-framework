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


import com.graphaware.propertycontainer.dto.string.StringConverter;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

/**
 * An simple implementation of {@link SerializableProperties}.
 */
public class SerializablePropertiesImpl extends ImmutablePropertiesImpl implements SerializableProperties {

    private static final StringConverter<Map<String, String>> STRING_CONVERTER = new StringPropertiesConverter();

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    public SerializablePropertiesImpl(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    public SerializablePropertiesImpl(Map<String, ?> properties) {
        super(properties);
    }

    /**
     * Construct a representation of properties from a {@link String}.
     *
     * @param string    to construct properties from. Must be of the form key1#value1#key2#value2... (assuming # separator).
     * @param separator of keys and values, ideally a single character, must not be null or empty.
     */
    public SerializablePropertiesImpl(String string, String separator) {
        super(STRING_CONVERTER.fromString(string, separator));
    }

    /**
     * Construct a representation of properties from a {@link String}.
     *
     * @param string    to construct properties from. Must be of the form prefix + key1#value1#key2#value2... (assuming # separator).
     * @param prefix    of the string that should be removed before conversion.
     * @param separator of keys and values, ideally a single character, must not be null or empty.
     */
    public SerializablePropertiesImpl(String string, String prefix, String separator) {
        super(STRING_CONVERTER.fromString(string, prefix, separator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(String prefix, String separator) {
        return STRING_CONVERTER.toString(this.getProperties(), prefix, separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(String separator) {
        return STRING_CONVERTER.toString(this.getProperties(), separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return STRING_CONVERTER.toString(this.getProperties(), "#");
    }
}
