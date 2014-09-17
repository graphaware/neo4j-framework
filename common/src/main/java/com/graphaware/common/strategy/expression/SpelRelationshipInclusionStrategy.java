package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.NodeCentricRelationshipInclusionStrategy;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link com.graphaware.common.strategy.RelationshipInclusionStrategy} based on a SPEL expression. The expression can
 * use methods defined in {@link RelationshipExpressions}.
 * <p/>
 * Note that there are certain methods (like {@link com.graphaware.common.strategy.expression.RelationshipExpressions#getOtherNode()}
 * or {@link com.graphaware.common.strategy.expression.RelationshipExpressions#isOutgoing()}) that rely on providing
 * a node which point of view the call is being made. These methods only work when calling {@link #include(org.neo4j.graphdb.Relationship, org.neo4j.graphdb.Node)}.
 * {@link IllegalArgumentException} is thrown when an incompatible method is invoked.
 */
public class SpelRelationshipInclusionStrategy extends SpelInclusionStrategy implements NodeCentricRelationshipInclusionStrategy {

    public SpelRelationshipInclusionStrategy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship) {
        return (Boolean) exp.getValue(new RelationshipExpressions(relationship));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Relationship relationship, Node pointOfView) {
        return (Boolean) exp.getValue(new RelationshipExpressions(relationship, pointOfView));
    }
}
