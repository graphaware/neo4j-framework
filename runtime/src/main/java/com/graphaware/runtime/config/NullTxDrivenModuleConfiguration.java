package com.graphaware.runtime.config;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.strategy.InclusionStrategies;

/**
 * {@link TxDrivenModuleConfiguration} for {@link com.graphaware.runtime.module.TxDrivenModule}s with
 * no configuration. Singleton.
 */
public final class NullTxDrivenModuleConfiguration implements TxDrivenModuleConfiguration {

    private static final TxDrivenModuleConfiguration INSTANCE = new NullTxDrivenModuleConfiguration();

    public static TxDrivenModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullTxDrivenModuleConfiguration() {
        Serializer.register(NullTxDrivenModuleConfiguration.class);
    }

    @Override
    public InclusionStrategies getInclusionStrategies() {
        return InclusionStrategies.all();
    }
}
