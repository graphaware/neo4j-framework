package com.graphaware.runtime.strategy;

import com.graphaware.common.strategy.InclusionStrategies;

/**
 * Factory for {@link InclusionStrategies}.
 */
public final class InclusionStrategiesFactory {

    private InclusionStrategiesFactory() {
    }

    /**
     * Produce {@link InclusionStrategies} that do not include internal nodes, relationships, and properties.
     *
     * @return a strategy that includes all nodes, relationships, and properties, except framework internal ones.
     */
    public static InclusionStrategies allBusiness() {
        return new InclusionStrategies(
                new IncludeAllBusinessNodes(),
                new IncludeAllBusinessNodeProperties(),
                new IncludeAllBusinessRelationships(),
                new IncludeAllBusinessRelationshipProperties()
        );
    }
}
