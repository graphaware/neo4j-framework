package com.graphaware.common.policy.spel;

import com.graphaware.common.policy.NodePropertyInclusionPolicy;
import org.neo4j.graphdb.Node;

/**
 * {@link NodePropertyInclusionPolicy} based on a SPEL expression. The expression can use methods defined in
 * {@link NodePropertyExpressions}.
 */
public class SpelNodePropertyInclusionPolicy extends SpelInclusionPolicy implements NodePropertyInclusionPolicy {

    public SpelNodePropertyInclusionPolicy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, Node node) {
        return (Boolean) exp.getValue(new NodePropertyExpressions(key, node));
    }
}
