package com.graphaware.common.policy.spel;

import org.neo4j.graphdb.Relationship;

/**
 *  {@link PropertyExpressions} for {@link Relationship} properties.
 */
class RelationshipPropertyExpressions extends PropertyExpressions<Relationship> {

    RelationshipPropertyExpressions(String key, Relationship relationship) {
        super(key, relationship);
    }

    public RelationshipExpressions getRelationship() {
        return new RelationshipExpressions(propertyContainer);
    }
}
