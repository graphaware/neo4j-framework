package com.graphaware.runtime.config;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;

/**
 * {@link TxDrivenModuleConfiguration} for {@link com.graphaware.runtime.module.TxDrivenModule}s with no configuration. Singleton.
 */
public final class NullTxDrivenModuleConfiguration implements TxDrivenModuleConfiguration {

    static {
       Serializer.register(NullTxDrivenModuleConfiguration.class, new SingletonSerializer(), 1000);
    }

    private static final TxDrivenModuleConfiguration INSTANCE = new NullTxDrivenModuleConfiguration();
    private final InclusionPolicies inclusionPolicies;

    /**
     * Get instance of this singleton configuration.
     *
     * @return instance.
     */
    public static TxDrivenModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullTxDrivenModuleConfiguration() {
        inclusionPolicies = InclusionPoliciesFactory.allBusiness();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InclusionPolicies getInclusionPolicies() {
        return inclusionPolicies;
    }
}
