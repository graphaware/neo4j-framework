package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import org.neo4j.graphdb.Relationship;

/**
 * {@link RelationshipPropertyInclusionStrategy} based on a SPEL expression. The expression can use methods defined in
 * {@link RelationshipPropertyExpressions}.
 */
public class SpelRelationshipPropertyInclusionStrategy extends SpelInclusionStrategy implements RelationshipPropertyInclusionStrategy {

    public SpelRelationshipPropertyInclusionStrategy(String expression) {
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
