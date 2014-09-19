package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.NodeInclusionPolicy;
import org.neo4j.graphdb.Node;

/**
 * {@link NodeInclusionPolicy} based on a SPEL expression. The expression can use methods defined in {@link NodeExpressions}.
 */
public class SpelNodeInclusionPolicy extends SpelInclusionPolicy implements NodeInclusionPolicy {

    public SpelNodeInclusionPolicy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node node) {
        return (Boolean) exp.getValue(new NodeExpressions(node));
    }
}
