package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.NodePropertyInclusionStrategy;
import org.neo4j.graphdb.Node;

/**
 * {@link NodePropertyInclusionStrategy} based on a SPEL expression. The expression can use methods defined in
 * {@link NodePropertyExpressions}.
 */
public class SpelNodePropertyInclusionStrategy extends SpelInclusionStrategy implements NodePropertyInclusionStrategy {

    public SpelNodePropertyInclusionStrategy(String expression) {
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
