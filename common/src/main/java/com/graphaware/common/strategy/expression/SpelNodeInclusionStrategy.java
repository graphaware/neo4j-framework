package com.graphaware.common.strategy.expression;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import org.neo4j.graphdb.Node;

/**
 * {@link NodeInclusionStrategy} based on a SPEL expression. The expression can use methods defined in
 * {@link NodeExpressions}.
 */
public class SpelNodeInclusionStrategy extends SpelInclusionStrategy implements NodeInclusionStrategy {

    public SpelNodeInclusionStrategy(String expression) {
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
