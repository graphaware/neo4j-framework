/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.common.policy.spel;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import static com.graphaware.common.util.DirectionUtils.resolveDirection;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * {@link PropertyContainerExpressions} for {@link Relationship}s.
 */
class RelationshipExpressions extends PropertyContainerExpressions<Relationship> {

    private final Node pointOfView;

    RelationshipExpressions(Relationship relationship) {
        this(relationship, null);
    }

    RelationshipExpressions(Relationship relationship, Node pointOfView) {
        super(relationship);
        this.pointOfView = pointOfView;
    }

    public NodeExpressions getStartNode() {
        return new NodeExpressions(propertyContainer.getStartNode());
    }

    public NodeExpressions getEndNode() {
        return new NodeExpressions(propertyContainer.getEndNode());
    }

    public NodeExpressions getOtherNode() {
        if (pointOfView == null) {
            throw new IllegalStateException("Relationship expression contains a reference to other node, but no reference is provided to this node.");
        }
        return new NodeExpressions(propertyContainer.getOtherNode(pointOfView));
    }

    public String getType() {
        return propertyContainer.getType().name();
    }

    public boolean isType(String type) {
        return propertyContainer.isType(withName(type));
    }

    public boolean isOutgoing() {
        if (pointOfView == null) {
            throw new IllegalStateException("Relationship expression contains a direction, but no information is available as to which node is looking.");
        }
        return OUTGOING.equals(resolveDirection(propertyContainer, pointOfView, OUTGOING));
    }

    public boolean isIncoming() {
        if (pointOfView == null) {
            throw new IllegalStateException("Relationship expression contains a direction, but no information is available as to which node is looking.");
        }
        return INCOMING.equals(resolveDirection(propertyContainer, pointOfView, OUTGOING));
    }
}
