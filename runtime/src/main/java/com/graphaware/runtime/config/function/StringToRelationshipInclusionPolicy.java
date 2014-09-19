package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.RelationshipInclusionPolicy;
import com.graphaware.common.policy.composite.CompositeRelationshipInclusionPolicy;
import com.graphaware.common.policy.spel.SpelRelationshipInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link com.graphaware.common.policy.RelationshipInclusionPolicy}. Singleton.
 */
public final class StringToRelationshipInclusionPolicy extends StringToInclusionPolicy<RelationshipInclusionPolicy> {

    private static StringToRelationshipInclusionPolicy INSTANCE = new StringToRelationshipInclusionPolicy();

    public static StringToRelationshipInclusionPolicy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipInclusionPolicy compositePolicy(RelationshipInclusionPolicy policy) {
        return CompositeRelationshipInclusionPolicy.of(IncludeAllBusinessRelationships.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipInclusionPolicy spelPolicy(String spel) {
        return new SpelRelationshipInclusionPolicy(spel);
    }
}
