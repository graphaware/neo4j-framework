package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.RelationshipInclusionPolicy;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link RelationshipInclusionPolicy} based on a SPEL expression. The expression can use methods defined in
 * {@link RelationshipExpressions}.
 * <p/>
 * Note that there are certain methods (like {@link RelationshipExpressions#getOtherNode()}
 * or {@link RelationshipExpressions#isOutgoing()}) that rely on providing
 * a node whose point of view the call is being made. These methods only work when calling {@link #include(org.neo4j.graphdb.Relationship, org.neo4j.graphdb.Node)}.
 * {@link IllegalArgumentException} is thrown when an incompatible method is invoked.
 */
public class SpelRelationshipInclusionPolicy extends SpelInclusionPolicy implements RelationshipInclusionPolicy {

    public SpelRelationshipInclusionPolicy(String expression) {
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
