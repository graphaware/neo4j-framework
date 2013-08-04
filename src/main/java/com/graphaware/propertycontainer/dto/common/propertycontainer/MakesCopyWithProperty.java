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

/**
 * Interface for (typically immutable) components that contain some properties and are able to return a copy
 * of themselves (again immutable), with a single property added.
 *
 * @param <T> type of object returned without a property.
 */
public interface MakesCopyWithProperty<T extends MakesCopyWithProperty<T>> {

    /**
     * Make a copy of this instance with an extra key-value pair.
     *
     * @param key   new key
     * @param value new value
     * @return copy of this instance with an extra key-value pair.
     */
    public T with(String key, Object value);
}
