package com.graphaware.runtime.config.function;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.expression.SpelNodeInclusionStrategy;

/**
 * A {@link StringToInclusionStrategy} that converts String to {@link NodeInclusionStrategy}. Singleton.
 */
public final class StringToNodeInclusionStrategy extends StringToInclusionStrategy<NodeInclusionStrategy> {

    private static StringToNodeInclusionStrategy INSTANCE = new StringToNodeInclusionStrategy();

    public static StringToNodeInclusionStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeInclusionStrategy spelStrategy(String spel) {
        return new SpelNodeInclusionStrategy(spel);
    }
}
