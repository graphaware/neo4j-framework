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

package com.graphaware.propertycontainer.dto.common.relationship;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * The simplest possible implementation of {@link HasTypeAndDirection}. Could be used as directed {@link org.neo4j.graphdb.Relationship}
 * representation when not interested in properties.
 */
public class TypeAndDirection extends BaseHasTypeAndDirection implements HasTypeAndDirection {

    /**
     * Construct a relationship representation. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     */
    public TypeAndDirection(Relationship relationship, Node pointOfView) {
        super(relationship, pointOfView);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type      type.
     * @param direction direction.
     */
    public TypeAndDirection(RelationshipType type, Direction direction) {
        super(type, direction);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    public TypeAndDirection(HasTypeAndDirection relationship) {
        super(relationship);
    }
}
