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

import java.util.Map;

/**
 * A simple implementation of {@link CopyMakingSerializableProperties}.
 */
public class CopyMakingSerializablePropertiesImpl extends BaseCopyMakingSerializableProperties<CopyMakingSerializablePropertiesImpl> implements CopyMakingSerializableProperties<CopyMakingSerializablePropertiesImpl> {

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    public CopyMakingSerializablePropertiesImpl(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    public CopyMakingSerializablePropertiesImpl(Map<String, ?> properties) {
        super(properties);
    }

    /**
     * Construct a representation of properties from a {@link String}.
     *
     * @param string    to construct properties from. Must be of the form key1#value1#key2#value2... (assuming # separator).
     * @param separator of keys and values, ideally a single character, must not be null or empty.
     */
    public CopyMakingSerializablePropertiesImpl(String string, String separator) {
        super(string, separator);
    }

    /**
     * Construct a representation of properties from a {@link String}.
     *
     * @param string    to construct properties from. Must be of the form prefix + key1#value1#key2#value2... (assuming # separator).
     * @param prefix    of the string that should be removed before conversion.
     * @param separator of keys and values, ideally a single character, must not be null or empty.
     */
    public CopyMakingSerializablePropertiesImpl(String string, String prefix, String separator) {
        super(string, prefix, separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CopyMakingSerializablePropertiesImpl newInstance(Map<String, String> props) {
        return new CopyMakingSerializablePropertiesImpl(props);
    }
}
