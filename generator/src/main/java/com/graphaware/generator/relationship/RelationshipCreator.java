package com.graphaware.generator.relationship;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * A component creating {@link org.neo4j.graphdb.Relationship}s with properties.
 */
public interface RelationshipCreator {

    /**
     * Create a relationship between two nodes with properties.
     *
     * @param first  first node.
     * @param second second node.
     * @return created relationship.
     */
    Relationship createRelationship(Node first, Node second);
}
