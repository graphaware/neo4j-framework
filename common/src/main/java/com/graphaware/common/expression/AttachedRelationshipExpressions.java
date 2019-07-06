/*
 * Copyright (c) 2013-2019 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.expression;

import com.graphaware.common.util.DirectionUtils;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

public interface AttachedRelationshipExpressions<N extends AttachedNodeExpressions> extends DetachedRelationshipExpressions {

    N getStartNode();

    N getEndNode();

    N pointOfView();

    default N getOtherNode() {
        if (pointOfView() == null) {
            throw new IllegalStateException("Relationship expression contains a reference to other node, but no reference is provided to this node.");
        }

        if (pointOfView().equals(getStartNode())) {
            return getEndNode();
        }

        if (pointOfView().equals(getEndNode())) {
            return getStartNode();
        }

        throw new IllegalStateException("Neither start node nor end node are the point of view. This is a bug");
    }

    default boolean isOutgoing() {
        if (pointOfView() == null) {
            throw new IllegalStateException("Relationship expression contains a direction, but no information is available as to which node is looking.");
        }
        return OUTGOING.equals(DirectionUtils.resolveDirection(this, pointOfView(), OUTGOING));
    }

    default boolean isIncoming() {
        if (pointOfView() == null) {
            throw new IllegalStateException("Relationship expression contains a direction, but no information is available as to which node is looking.");
        }
        return INCOMING.equals(DirectionUtils.resolveDirection(this, pointOfView(), OUTGOING));
    }
}
