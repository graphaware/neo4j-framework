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

import com.graphaware.propertycontainer.dto.common.property.ImmutableProperties;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

/**
 * A simple (immutable) implementation of {@link com.graphaware.propertycontainer.dto.common.property.ImmutableProperties}  with {@link String} values.
 */
public class ImmutablePropertiesImpl extends StringProperties implements ImmutableProperties<String> {

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    public ImmutablePropertiesImpl(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    public ImmutablePropertiesImpl(Map<String, ?> properties) {
        super(properties);
    }

    /**
     * @throws UnsupportedOperationException always, this is an immutable class.
     */
    @Override
    protected final void setProperty(String key, String value) {
        throw new UnsupportedOperationException(ImmutablePropertiesImpl.class.getSimpleName() + " is immutable!");
    }

    /**
     * @throws UnsupportedOperationException always, this is an immutable class.
     */
    @Override
    protected final boolean removeProperty(String key) {
        throw new UnsupportedOperationException(ImmutablePropertiesImpl.class.getSimpleName() + " is immutable!");
    }
}
