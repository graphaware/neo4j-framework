package com.graphaware.neo4j.framework.strategy;

import com.graphaware.neo4j.tx.event.strategy.*;

import java.lang.Override;

/**
 * {@link com.graphaware.neo4j.tx.event.strategy.InclusionStrategies}, providing static factory method for default
 * "all"/"none" configurations and "with" methods for fluently overriding these with custom strategies.
 */
public class InclusionStrategiesImpl extends BaseInclusionStrategies<InclusionStrategiesImpl> implements InclusionStrategies {

    /**
     * Create all-including strategies.
     *
     * @return all-including strategies.
     */
    public static InclusionStrategiesImpl all() {
        return new InclusionStrategiesImpl(
                IncludeAllNodes.getInstance(),
                IncludeAllNodeProperties.getInstance(),
                IncludeAllRelationships.getInstance(),
                IncludeAllRelationshipProperties.getInstance());
    }

    /**
     * Create nothing-including strategies.
     *
     * @return nothing-including strategies.
     */
    public static InclusionStrategiesImpl none() {
        return new InclusionStrategiesImpl(
                IncludeNoNodes.getInstance(),
                IncludeNoNodeProperties.getInstance(),
                IncludeNoRelationships.getInstance(),
                IncludeNoRelationshipProperties.getInstance());
    }

    /**
     * Constructor.
     *
     * @param nodeInclusionStrategy         strategy.
     * @param nodePropertyInclusionStrategy strategy.
     * @param relationshipInclusionStrategy strategy.
     * @param relationshipPropertyInclusionStrategy
     *                                      strategy.
     */
    private InclusionStrategiesImpl(NodeInclusionStrategy nodeInclusionStrategy, NodePropertyInclusionStrategy nodePropertyInclusionStrategy, com.graphaware.neo4j.tx.event.strategy.RelationshipInclusionStrategy relationshipInclusionStrategy, RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        super(nodeInclusionStrategy, nodePropertyInclusionStrategy, relationshipInclusionStrategy, relationshipPropertyInclusionStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InclusionStrategiesImpl newInstance(NodeInclusionStrategy nodeInclusionStrategy, NodePropertyInclusionStrategy nodePropertyInclusionStrategy, RelationshipInclusionStrategy relationshipInclusionStrategy, RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        return new InclusionStrategiesImpl(nodeInclusionStrategy, nodePropertyInclusionStrategy, relationshipInclusionStrategy, relationshipPropertyInclusionStrategy);
    }
}
