package com.graphaware.runtime.config.function;

import com.graphaware.common.strategy.NodePropertyInclusionStrategy;
import com.graphaware.common.strategy.expression.SpelNodePropertyInclusionStrategy;

/**
 * A {@link StringToInclusionStrategy} that converts String to {@link NodePropertyInclusionStrategy}. Singleton.
 */
public final class StringToNodePropertyInclusionStrategy extends StringToInclusionStrategy<NodePropertyInclusionStrategy> {

    private static StringToNodePropertyInclusionStrategy INSTANCE = new StringToNodePropertyInclusionStrategy();

    public static StringToNodePropertyInclusionStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodePropertyInclusionStrategy spelStrategy(String spel) {
        return new SpelNodePropertyInclusionStrategy(spel);
    }
}
