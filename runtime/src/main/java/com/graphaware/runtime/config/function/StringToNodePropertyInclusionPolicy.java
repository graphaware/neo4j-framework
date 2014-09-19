package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.NodePropertyInclusionPolicy;
import com.graphaware.common.policy.all.IncludeAllNodeProperties;
import com.graphaware.common.policy.composite.CompositeNodePropertyInclusionPolicy;
import com.graphaware.common.policy.spel.SpelNodePropertyInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodeProperties;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link com.graphaware.common.policy.NodePropertyInclusionPolicy}. Singleton.
 */
public final class StringToNodePropertyInclusionPolicy extends StringToInclusionPolicy<NodePropertyInclusionPolicy> {

    private static StringToNodePropertyInclusionPolicy INSTANCE = new StringToNodePropertyInclusionPolicy();

    public static StringToNodePropertyInclusionPolicy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodePropertyInclusionPolicy compositePolicy(NodePropertyInclusionPolicy policy) {
        return CompositeNodePropertyInclusionPolicy.of(IncludeAllBusinessNodeProperties.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodePropertyInclusionPolicy spelPolicy(String spel) {
        return new SpelNodePropertyInclusionPolicy(spel);
    }
}
