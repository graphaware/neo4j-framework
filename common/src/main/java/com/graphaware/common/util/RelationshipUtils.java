package com.graphaware.common.util;

import org.neo4j.graphdb.*;

import static org.neo4j.graphalgo.GraphAlgoFactory.shortestPath;
import static org.neo4j.graphdb.PathExpanders.forTypeAndDirection;

/**
 * Utility methods for dealing with {@link org.neo4j.graphdb.Relationship}s.
 */
public final class RelationshipUtils {

    /**
     * Get a single relationship between two nodes.
     *
     * @param node1     first node.
     * @param node2     second node.
     * @param type      relationship type.
     * @param direction relationship direction from first node's point of view (can be BOTH).
     * @return a single relationship. If there is
     *         more than one relationship, one (unspecified which one) will be returned.
     * @throws NotFoundException if there is no such relationship.
     */
    public static Relationship getSingleRelationship(Node node1, Node node2, RelationshipType type, Direction direction) {
        Relationship result = getSingleRelationshipOrNull(node1, node2, type, direction);

        if (result == null) {
            throw new NotFoundException("Relationship between " + node1 + " and " + node2 + " of type " + type + " and direction " + direction + " does not exist.");
        }

        return result;
    }

    /**
     * Get a single relationship between two nodes.
     *
     * @param node1     first node.
     * @param node2     second node.
     * @param type      relationship type.
     * @param direction relationship direction from first node's point of view (can be BOTH).
     * @return a single relationship, null if there is no such relationship between the nodes. If there is
     *         more than one relationship, one (unspecified which one) will be returned.
     */
    public static Relationship getSingleRelationshipOrNull(Node node1, Node node2, RelationshipType type, Direction direction) {
        Path singlePath = shortestPath(forTypeAndDirection(type, direction), 1, 1).findSinglePath(node1, node2);

        if (singlePath == null) {
            return null;
        }

        return singlePath.lastRelationship();
    }

    /**
     * Check that a relationship does not exist between two nodes.
     *
     * @param node1     first node.
     * @param node2     second node.
     * @param type      relationship type.
     * @param direction relationship direction from first node's point of view (can be BOTH).
     * @return true iff the specified relationship does not exist.
     */
    public static boolean relationshipNotExists(Node node1, Node node2, RelationshipType type, Direction direction) {
        return !relationshipExists(node1, node2, type, direction);
    }

    /**
     * Check that a relationship exists between two nodes.
     *
     * @param node1     first node.
     * @param node2     second node.
     * @param type      relationship type.
     * @param direction relationship direction from first node's point of view (can be BOTH).
     * @return true iff at least one relationship with the above spec exists.
     */
    public static boolean relationshipExists(Node node1, Node node2, RelationshipType type, Direction direction) {
        return getSingleRelationshipOrNull(node1, node2, type, direction) != null;
    }

    /**
     * Delete a relationship if one exists. Do nothing if the specified relationship does not exist.
     *
     * @param node1     first node.
     * @param node2     second node.
     * @param type      relationship type.
     * @param direction relationship direction from first node's point of view (can be BOTH).
     */
    public static void deleteRelationshipIfExists(Node node1, Node node2, RelationshipType type, Direction direction) {
        Relationship r = getSingleRelationshipOrNull(node1, node2, type, direction);

        if (r != null) {
            r.delete();
        }
    }

    /**
     * Create a relationship if one doesn't already exist. Do nothing if one does exist.
     *
     * @param node1     first node.
     * @param node2     second node.
     * @param type      relationship type.
     * @param direction relationship direction from first node's point of view (can be BOTH).
     * @return the new or the existing relationship.
     */
    public static Relationship createRelationshipIfNotExists(Node node1, Node node2, RelationshipType type, Direction direction) {
        Relationship existing = getSingleRelationshipOrNull(node1, node2, type, direction);

        if (existing == null) {
            return node1.createRelationshipTo(node2, type);
        }

        return existing;
    }

    private RelationshipUtils() {
    }
}
