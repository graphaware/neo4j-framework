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

package com.graphaware.propertycontainer.dto.string.relationship;

import com.graphaware.propertycontainer.dto.common.relationship.ImmutableRelationship;
import com.graphaware.propertycontainer.dto.string.Serializable;
import com.graphaware.propertycontainer.dto.string.property.SerializableProperties;

/**
 * {@link Serializable} {@link com.graphaware.propertycontainer.dto.common.relationship.ImmutableRelationship}
 *
 * @param <P> type of properties held by this relationship representation.
 */
public interface SerializableRelationship<P extends SerializableProperties> extends ImmutableRelationship<String, P>, Serializable {
}
