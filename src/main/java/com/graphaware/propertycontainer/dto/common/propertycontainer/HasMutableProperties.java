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

import com.graphaware.propertycontainer.dto.common.property.MutableProperties;

/**
 * Component that has {@link com.graphaware.propertycontainer.dto.common.property.MutableProperties}.
 *
 * @param <V> type with which property values are represented.
 * @param <P> type of properties contained.
 */
public interface HasMutableProperties<V, P extends MutableProperties<V>> extends HasProperties<V, P> {

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
