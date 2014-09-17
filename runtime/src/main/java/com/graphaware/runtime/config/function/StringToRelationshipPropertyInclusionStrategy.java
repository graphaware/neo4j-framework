package com.graphaware.runtime.config.function;

import com.graphaware.common.strategy.RelationshipPropertyInclusionStrategy;
import com.graphaware.common.strategy.expression.SpelRelationshipPropertyInclusionStrategy;

/**
 * A {@link StringToInclusionStrategy} that converts String to {@link RelationshipPropertyInclusionStrategy}. Singleton.
 */
public final class StringToRelationshipPropertyInclusionStrategy extends StringToInclusionStrategy<RelationshipPropertyInclusionStrategy> {

    private static StringToRelationshipPropertyInclusionStrategy INSTANCE = new StringToRelationshipPropertyInclusionStrategy();

    public static StringToRelationshipPropertyInclusionStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipPropertyInclusionStrategy spelStrategy(String spel) {
        return new SpelRelationshipPropertyInclusionStrategy(spel);
    }
}
