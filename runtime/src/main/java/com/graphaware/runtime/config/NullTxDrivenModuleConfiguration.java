package com.graphaware.runtime.config;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.strategy.InclusionStrategiesFactory;

/**
 * {@link TxDrivenModuleConfiguration} for {@link com.graphaware.runtime.module.TxDrivenModule}s with no configuration. Singleton.
 */
public final class NullTxDrivenModuleConfiguration implements TxDrivenModuleConfiguration {

    static {
       Serializer.register(NullTxDrivenModuleConfiguration.class, new SingletonSerializer(), 1000);
    }

    private static final TxDrivenModuleConfiguration INSTANCE = new NullTxDrivenModuleConfiguration();
    private final InclusionStrategies inclusionStrategies;

    /**
     * Get instance of this singleton configuration.
     *
     * @return instance.
     */
    public static TxDrivenModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullTxDrivenModuleConfiguration() {
        inclusionStrategies = InclusionStrategiesFactory.allBusiness();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InclusionStrategies getInclusionStrategies() {
        return inclusionStrategies;
    }
}
