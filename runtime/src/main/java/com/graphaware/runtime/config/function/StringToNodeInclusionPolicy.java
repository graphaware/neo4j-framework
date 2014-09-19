package com.graphaware.runtime.config.function;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.common.policy.composite.CompositeNodeInclusionPolicy;
import com.graphaware.common.policy.spel.SpelNodeInclusionPolicy;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;

/**
 * A {@link StringToInclusionPolicy} that converts String to {@link com.graphaware.common.policy.NodeInclusionPolicy}. Singleton.
 */
public final class StringToNodeInclusionPolicy extends StringToInclusionPolicy<NodeInclusionPolicy> {

    private static StringToNodeInclusionPolicy INSTANCE = new StringToNodeInclusionPolicy();

    public static StringToNodeInclusionPolicy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeInclusionPolicy compositePolicy(NodeInclusionPolicy policy) {
        return CompositeNodeInclusionPolicy.of(IncludeAllBusinessNodes.getInstance(), policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeInclusionPolicy spelPolicy(String spel) {
        return new SpelNodeInclusionPolicy(spel);
    }
}
