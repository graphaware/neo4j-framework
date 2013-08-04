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

import com.graphaware.propertycontainer.dto.common.property.BaseProperties;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

import static com.graphaware.propertycontainer.util.PropertyContainerUtils.cleanStringProperties;
import static com.graphaware.propertycontainer.util.PropertyContainerUtils.propertiesToStringMap;


/**
 * Abstract base-class for implementations of {@link com.graphaware.propertycontainer.dto.common.property.ImmutableProperties}
 * and {@link com.graphaware.propertycontainer.dto.common.property.MutableProperties} with {@link String} values.
 */
public abstract class StringProperties extends BaseProperties<String> {

    /**
     * Construct a representation of properties from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to take (copy) properties from.
     */
    protected StringProperties(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a representation of properties from a {@link java.util.Map}.
     *
     * @param properties to take (copy).
     */
    protected StringProperties(Map<String, ?> properties) {
        super(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> propertiesToMap(PropertyContainer propertyContainer) {
        return propertiesToStringMap(propertyContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> cleanProperties(Map<String, ?> properties) {
        return cleanStringProperties(properties);
    }

    /**
     * Do these properties match (are they the same as) the properties held by the given {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to check.
     * @return true iff the properties represented by this object match the properties held by the given {@link org.neo4j.graphdb.PropertyContainer}.
     */
    public boolean matches(PropertyContainer propertyContainer) {
        return propertiesToStringMap(propertyContainer).equals(getProperties());
    }
}
