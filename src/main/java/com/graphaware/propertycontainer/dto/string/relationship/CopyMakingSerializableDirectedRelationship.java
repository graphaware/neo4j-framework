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

import com.graphaware.propertycontainer.dto.common.propertycontainer.MakesCopyWithProperty;
import com.graphaware.propertycontainer.dto.common.propertycontainer.MakesCopyWithoutProperty;
import com.graphaware.propertycontainer.dto.string.property.CopyMakingSerializableProperties;

/**
 * {@link SerializableDirectedRelationship} that {@link MakesCopyWithProperty} and {@link MakesCopyWithoutProperty}.
 *
 * @param <P> type of properties held by this relationship representation.
 * @param <R> type of relationship returned with and without property.
 */
public interface CopyMakingSerializableDirectedRelationship<P extends CopyMakingSerializableProperties<P>, R extends CopyMakingSerializableDirectedRelationship<P, R>> extends SerializableDirectedRelationship<P>, MakesCopyWithProperty<R>, MakesCopyWithoutProperty<R> {
}
