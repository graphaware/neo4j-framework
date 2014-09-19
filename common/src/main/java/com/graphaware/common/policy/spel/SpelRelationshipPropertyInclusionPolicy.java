package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.RelationshipPropertyInclusionPolicy;
import org.neo4j.graphdb.Relationship;

/**
 * {@link RelationshipPropertyInclusionPolicy} based on a SPEL expression. The expression can use methods defined in
 * {@link RelationshipPropertyExpressions}.
 */
public class SpelRelationshipPropertyInclusionPolicy extends SpelInclusionPolicy implements RelationshipPropertyInclusionPolicy {

    public SpelRelationshipPropertyInclusionPolicy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, Relationship relationship) {
        return (Boolean) exp.getValue(new RelationshipPropertyExpressions(key, relationship));
    }
}
