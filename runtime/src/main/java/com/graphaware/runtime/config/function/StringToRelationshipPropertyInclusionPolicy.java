package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.RelationshipPropertyInclusionPolicy;
import com.graphaware.common.policy.composite.CompositeRelationshipPropertyInclusionPolicy;
import com.graphaware.common.policy.spel.SpelRelationshipPropertyInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationshipProperties;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link com.graphaware.common.policy.RelationshipPropertyInclusionPolicy}. Singleton.
 */
public final class StringToRelationshipPropertyInclusionPolicy extends StringToInclusionPolicy<RelationshipPropertyInclusionPolicy> {

    private static StringToRelationshipPropertyInclusionPolicy INSTANCE = new StringToRelationshipPropertyInclusionPolicy();

    public static StringToRelationshipPropertyInclusionPolicy getInstance() {
        return INSTANCE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipPropertyInclusionPolicy compositePolicy(RelationshipPropertyInclusionPolicy policy) {
        return CompositeRelationshipPropertyInclusionPolicy.of(IncludeAllBusinessRelationshipProperties.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RelationshipPropertyInclusionPolicy spelPolicy(String spel) {
        return new SpelRelationshipPropertyInclusionPolicy(spel);
    }
}
