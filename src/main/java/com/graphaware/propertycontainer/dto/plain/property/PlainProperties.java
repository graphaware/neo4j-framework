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

import com.graphaware.propertycontainer.dto.common.property.BaseProperties;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

import static com.graphaware.propertycontainer.util.ArrayUtils.arrayFriendlyHasCode;
import static com.graphaware.propertycontainer.util.ArrayUtils.arrayFriendlyMapEquals;
import static com.graphaware.propertycontainer.util.PropertyContainerUtils.cleanObjectProperties;
import static com.graphaware.propertycontainer.util.PropertyContainerUtils.propertiesToObjectMap;


/**
 * Base-class for implementations of {@link com.graphaware.propertycontainer.dto.common.property.ImmutableProperties} with {@link Object} values.
 */
public abstract class PlainProperties extends BaseProperties<Object> {

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    protected PlainProperties(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    protected PlainProperties(Map<String, ?> properties) {
        super(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> propertiesToMap(PropertyContainer propertyContainer) {
        return propertiesToObjectMap(propertyContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> cleanProperties(Map<String, ?> properties) {
        return cleanObjectProperties(properties);
    }

    /**
     * Do these properties match (are they the same as) the properties held by the given {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to check.
     * @return true iff the properties represented by this object match the properties held by the given {@link org.neo4j.graphdb.PropertyContainer}.
     */
    public boolean matches(PropertyContainer propertyContainer) {
        return arrayFriendlyMapEquals(propertiesToObjectMap(propertyContainer), getProperties());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return arrayFriendlyMapEquals(getProperties(), ((PlainProperties) o).getProperties());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int h = 0;
        for (Map.Entry<String, Object> stringObjectEntry : entrySet()) h += arrayFriendlyHasCode(stringObjectEntry);
        return h;
    }
}
