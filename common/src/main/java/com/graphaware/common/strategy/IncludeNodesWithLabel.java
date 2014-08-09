package com.graphaware.common.strategy;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 * {@link NodeInclusionStrategy} that only includes {@link Node}s with a given {@link Label}.
 */
public class IncludeNodesWithLabel implements NodeInclusionStrategy {

    private final Label label;

    /**
     * Construct a new inclusion strategy.
     *
     * @param label nodes have to have to be included.
     */
    public IncludeNodesWithLabel(Label label) {
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node node) {
        return node.hasLabel(label);
    }
}
