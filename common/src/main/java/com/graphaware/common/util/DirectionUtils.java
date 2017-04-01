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

package com.graphaware.common.util;

import com.graphaware.common.expression.AttachedNodeExpressions;
import com.graphaware.common.expression.AttachedRelationshipExpressions;
import com.graphaware.common.log.LoggerFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;

import static org.neo4j.graphdb.Direction.*;

/**
 * Utility class for static methods related to {@link org.neo4j.graphdb.Relationship} {@link org.neo4j.graphdb.Direction}.
 */
public final class DirectionUtils {
    private static final Log LOG = LoggerFactory.getLogger(DirectionUtils.class);

    /**
     * Resolve the direction of a relationship from a node's point of view. If the start and end nodes of the relationship
     * are the same node, {@link org.neo4j.graphdb.Direction#BOTH} will be returned.
     *
     * @param relationship to resolve direction for.
     * @param pointOfView  direction will be from this node's point of view.
     * @return direction of the relationship from the given node's point of view.
     */
    public static Direction resolveDirection(Relationship relationship, Node pointOfView) {
        return resolveDirection(relationship, pointOfView, BOTH);
    }

    /**
     * Resolve the direction of a relationship from a node's point of view.
     *
     * @param relationship     to resolve direction for.
     * @param pointOfView      direction will be from this node's point of view.
     * @param defaultDirection value returned in case the relationship is a "self-relationships", i.e. the start and
     *                         end nodes are the same.
     * @return direction of the relationship from the given node's point of view.
     */
    public static Direction resolveDirection(Relationship relationship, Node pointOfView, Direction defaultDirection) {
        if (relationship.getEndNode().getId() != pointOfView.getId() && relationship.getStartNode().getId() != pointOfView.getId()) {
            String message = "Provided relationship (" + relationship.getId() + ") does not have node (" + pointOfView.getId() + ") on either of its ends!";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }

        if (relationship.getEndNode().getId() == relationship.getStartNode().getId()) {
            return defaultDirection;
        }

        return ((relationship.getStartNode().getId() == pointOfView.getId()) ? OUTGOING : INCOMING);
    }

    /**
     * Resolve the direction of a relationship from a node's point of view.
     *
     * @param relationship     to resolve direction for.
     * @param pointOfView      direction will be from this node's point of view.
     * @param defaultDirection value returned in case the relationship is a "self-relationships", i.e. the start and
     *                         end nodes are the same.
     * @return direction of the relationship from the given node's point of view.
     */
    public static Direction resolveDirection(AttachedRelationshipExpressions<?> relationship, AttachedNodeExpressions pointOfView, Direction defaultDirection) {
        if (!relationship.getEndNode().equals(pointOfView) && !relationship.getStartNode().equals(pointOfView)) {
            String message = "Provided relationship (" + relationship + ") does not have node (" + pointOfView + ") on either of its ends!";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }

        if (relationship.getEndNode().equals(relationship.getStartNode())) {
            return defaultDirection;
        }

        return ((relationship.getStartNode().equals(pointOfView) ? OUTGOING : INCOMING));
    }

    /**
     * Does the given relationship match the given direction from the given node's point of view? Directions match if
     * either they are the same, or at least one of them is {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship for which to check direction match.
     * @param pointOfView  direction of the above relationship will be resolved from this node's point of view.
     *                     If it is resolved to {@link org.neo4j.graphdb.Direction#BOTH}, then the method always returns <code>true</code>.
     * @param direction    to match. If {@link org.neo4j.graphdb.Direction#BOTH}, then the method always returns <code>true</code>.
     * @return true iff the relationship matches the direction from the given node's point of view.
     */
    public static boolean matches(Relationship relationship, Node pointOfView, Direction direction) {
        return matches(direction, resolveDirection(relationship, pointOfView, BOTH));
    }

    /**
     * Do the two directions match?
     *
     * @param direction1 one
     * @param direction2 two
     * @return true iff at least one of the directions is {@link org.neo4j.graphdb.Direction#BOTH} or they are equal.
     */
    public static boolean matches(Direction direction1, Direction direction2) {
        return BOTH.equals(direction1) || BOTH.equals(direction2) || direction1.equals(direction2);
    }

    /**
     * Reverse direction.
     *
     * @param direction to reverse.
     * @return reversed direction.
     */
    public static Direction reverse(Direction direction) {
        switch (direction) {
            case BOTH:
                return BOTH;
            case OUTGOING:
                return INCOMING;
            case INCOMING:
                return OUTGOING;
            default:
                throw new IllegalArgumentException("Unknown direction " + direction);
        }
    }

    private DirectionUtils() {
    }
}
