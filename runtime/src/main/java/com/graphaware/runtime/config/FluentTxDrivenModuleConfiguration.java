package com.graphaware.runtime.config;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;

/**
 * {@link TxDrivenModuleConfiguration} with fluent interface.
 * Intended for users of Neo4j in embedded mode to programatically configure the runtime.
 */
public class FluentTxDrivenModuleConfiguration extends BaseTxDrivenModuleConfiguration<FluentTxDrivenModuleConfiguration> {

    /**
     * Creates an instance with default values, i.e., with {@link com.graphaware.runtime.policy.InclusionPoliciesFactory#allBusiness()}.
     *
     * @return The {@link FluentRuntimeConfiguration} instance.
     */
    public static FluentTxDrivenModuleConfiguration defaultConfiguration() {
        return new FluentTxDrivenModuleConfiguration();
    }

    /**
     * Create a new configuration with {@link com.graphaware.runtime.policy.InclusionPoliciesFactory#allBusiness()}.
     */
    private FluentTxDrivenModuleConfiguration() {
        super(InclusionPoliciesFactory.allBusiness());
    }

    /**
     * Create a new configuration.
     *
     * @param inclusionPolicies of the configuration.
     */
    private FluentTxDrivenModuleConfiguration(InclusionPolicies inclusionPolicies) {
        super(inclusionPolicies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FluentTxDrivenModuleConfiguration newInstance(InclusionPolicies inclusionPolicies) {
        return new FluentTxDrivenModuleConfiguration(inclusionPolicies);
    }
}
