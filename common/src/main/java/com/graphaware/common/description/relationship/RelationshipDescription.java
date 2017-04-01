/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.description.relationship;

import com.graphaware.common.description.MutuallyExclusive;
import com.graphaware.common.description.PartiallyComparable;
import com.graphaware.common.description.property.PropertiesDescription;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

/**
 * An immutable description of a relationship from a node's point of view.
 * It is {@link PartiallyComparable} and can judge, whether it is {@link MutuallyExclusive} with another one.
 */
public interface RelationshipDescription extends PartiallyComparable<RelationshipDescription>, MutuallyExclusive<RelationshipDescription> {

    /**
     * Get the relationship type.
     *
     * @return type.
     */
    String getType();

    /**
     * Get the relationship direction.
     *
     * @return direction. Note that this can be {@link org.neo4j.graphdb.Direction#BOTH}.
     */
    Direction getDirection();

    /**
     * Get the relationship's properties description.
     *
     * @return properties description.
     */
    PropertiesDescription getPropertiesDescription();
}
