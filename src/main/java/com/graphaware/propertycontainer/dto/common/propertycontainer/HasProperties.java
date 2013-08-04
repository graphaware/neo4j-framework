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

package com.graphaware.propertycontainer.dto.common.propertycontainer;

import com.graphaware.propertycontainer.dto.common.property.ImmutableProperties;
import org.neo4j.graphdb.PropertyContainer;

/**
 * Component that has {@link ImmutableProperties}.
 *
 * @param <V> type with which property values are represented.
 * @param <P> type of properties contained.
 */
public interface HasProperties<V, P extends ImmutableProperties<V>> {

    /**
     * Get the properties attached to the object.
     *
     * @return properties.
     */
    P getProperties();

    /**
     * Do the properties of this instance match (are they the same as) the properties held by the given
     * {@link org.neo4j.graphdb.PropertyContainer}?
     *
     * @param propertyContainer to check.
     * @return true iff the properties held by this instance match the properties held by the given
     *         {@link org.neo4j.graphdb.PropertyContainer}.
     */
    boolean matches(PropertyContainer propertyContainer);

    /**
     * Do the properties of this instance match (are they the same as) the properties held by the given
     * {@link HasProperties}?
     *
     * @param hasProperties to check.
     * @return true iff the properties held by this instance match the properties held by the given
     *         {@link HasProperties}.
     */
    boolean matches(HasProperties<V, ?> hasProperties);
}
