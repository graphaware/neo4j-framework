package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.NodeInclusionPolicy;
import org.neo4j.graphdb.Node;

/**
 * {@link CompositePropertyContainerInclusionPolicy} for {@link Node}s.
 */
public final class CompositeNodeInclusionPolicy extends CompositePropertyContainerInclusionPolicy<Node, NodeInclusionPolicy> implements NodeInclusionPolicy {

    public static CompositeNodeInclusionPolicy of(NodeInclusionPolicy... policies) {
        return new CompositeNodeInclusionPolicy(policies);
    }

    private CompositeNodeInclusionPolicy(NodeInclusionPolicy[] policies) {
        super(policies);
    }
}
