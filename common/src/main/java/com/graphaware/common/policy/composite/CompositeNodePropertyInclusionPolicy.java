package com.graphaware.common.policy.composite;

import com.graphaware.common.policy.NodePropertyInclusionPolicy;
import org.neo4j.graphdb.Node;

/**
 * {@link CompositePropertyInclusionPolicy} for {@link Node}s.
 */
public final class CompositeNodePropertyInclusionPolicy extends CompositePropertyInclusionPolicy<Node> implements NodePropertyInclusionPolicy {

    public static CompositeNodePropertyInclusionPolicy of(NodePropertyInclusionPolicy... policies) {
        return new CompositeNodePropertyInclusionPolicy(policies);
    }

    private CompositeNodePropertyInclusionPolicy(NodePropertyInclusionPolicy[] policies) {
        super(policies);
    }
}
