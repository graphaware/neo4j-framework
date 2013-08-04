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

package com.graphaware.propertycontainer.dto.plain.property;

import com.graphaware.propertycontainer.dto.common.property.MutableProperties;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Collections;
import java.util.Map;

/**
 * A simple implementation of {@link MutableProperties} with {@link Object} values.
 */
public class MutablePropertiesImpl extends PlainProperties implements MutableProperties<Object> {

    /**
     * Construct an empty representation of properties.
     */
    public MutablePropertiesImpl() {
        super(Collections.<String, Object>emptyMap());
    }

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    public MutablePropertiesImpl(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    public MutablePropertiesImpl(Map<String, ?> properties) {
        super(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        super.setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeProperty(String key) {
        return super.removeProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        super.clear();
    }
}
