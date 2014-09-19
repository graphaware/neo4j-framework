package com.graphaware.runtime.config;

import com.graphaware.common.policy.InclusionPolicies;

/**
 * Encapsulates all configuration of a single {@link com.graphaware.runtime.module.TxDrivenModule}. Modules that need
 * no configuration should use {@link NullTxDrivenModuleConfiguration}. Otherwise, start with {@link FluentTxDrivenModuleConfiguration}.
 */
public interface TxDrivenModuleConfiguration {

    /**
     * Get the inclusion policies used by this module. If unsure, return {@link com.graphaware.runtime.policy.InclusionPoliciesFactory#allBusiness()}.
     *
     * @return policies.
     */
    InclusionPolicies getInclusionPolicies();
}
