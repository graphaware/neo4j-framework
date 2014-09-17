package com.graphaware.runtime.config.function;

import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.common.strategy.expression.SpelRelationshipInclusionStrategy;

/**
 * A {@link StringToInclusionStrategy} that converts String to {@link RelationshipInclusionStrategy}. Singleton.
 */
public final class StringToRelationshipInclusionStrategy extends StringToInclusionStrategy<RelationshipInclusionStrategy> {

    private static StringToRelationshipInclusionStrategy INSTANCE = new StringToRelationshipInclusionStrategy();

    public static StringToRelationshipInclusionStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipInclusionStrategy spelStrategy(String spel) {
        return new SpelRelationshipInclusionStrategy(spel);
    }
}
