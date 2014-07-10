package com.graphaware.generator.relationship;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * A {@link RelationshipCreator} that only creates relationship of type FRIEND_OF and assigns no properties to those
 * relationships.
 */
public class SocialNetworkRelationshipCreator implements RelationshipCreator {

    private static final RelationshipType FRIEND_OF = DynamicRelationshipType.withName("FRIEND_OF");

    private static final SocialNetworkRelationshipCreator INSTANCE = new SocialNetworkRelationshipCreator();

    private SocialNetworkRelationshipCreator() {
    }

    public static SocialNetworkRelationshipCreator getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship createRelationship(Node first, Node second) {
        return first.createRelationshipTo(second, FRIEND_OF);
    }
}
