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
 * of themselves (again immutable), with a single property omitted.
 *
 * @param <T> type of object returned without a property.
 */
public interface MakesCopyWithoutProperty<T extends MakesCopyWithoutProperty<T>> {

    /**
     * Make a copy of this instance with a single key-value pair missing.
     *
     * @param key to omit in the copy. If the key does not exist in the representation, an identical copy will be returned.
     * @return copy of this instance except one key-value pair.
     */
    public T without(String key);
}
