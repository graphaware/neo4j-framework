package com.graphaware.runtime.config;

import com.graphaware.common.strategy.InclusionStrategies;

/**
 * {@link RuntimeModuleConfiguration} for {@link com.graphaware.runtime.GraphAwareRuntimeModule}s with no configuration.
 * Singleton.
 */
public final class NullRuntimeModuleConfiguration implements RuntimeModuleConfiguration {

    private static final RuntimeModuleConfiguration INSTANCE = new NullRuntimeModuleConfiguration();

    public static RuntimeModuleConfiguration getInstance() {
        return INSTANCE;
    }

    private NullRuntimeModuleConfiguration() {
    }

    @Override
    public InclusionStrategies getInclusionStrategies() {
        return InclusionStrategies.all();
    }
}
