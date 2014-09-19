package com.graphaware.runtime.policy;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodeProperties;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationshipProperties;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;

/**
 * Factory for {@link com.graphaware.common.policy.InclusionPolicies}.
 */
public final class InclusionPoliciesFactory {

    private InclusionPoliciesFactory() {
    }

    /**
     * Produce {@link com.graphaware.common.policy.InclusionPolicies} that do not include internal nodes, relationships, and properties.
     *
     * @return a policy that includes all nodes, relationships, and properties, except framework internal ones.
     */
    public static InclusionPolicies allBusiness() {
        return new InclusionPolicies(
                IncludeAllBusinessNodes.getInstance(),
                IncludeAllBusinessNodeProperties.getInstance(),
                IncludeAllBusinessRelationships.getInstance(),
                IncludeAllBusinessRelationshipProperties.getInstance()
        );
    }
}
