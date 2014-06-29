package com.graphaware.runtime.config;

import com.graphaware.common.strategy.InclusionStrategies;

/**
 * Encapsulates all configuration of a single {@link com.graphaware.runtime.module.TxDrivenModule}. Modules that need
 * no configuration should use {@link NullTxDrivenModuleConfiguration}.
 */
public interface TxDrivenModuleConfiguration {

    /**
     * Get the inclusion strategies used by this module. If unsure, return {@link com.graphaware.common.strategy.InclusionStrategies#all()}.
     *
     * @return strategies.
     */
    InclusionStrategies getInclusionStrategies();
}
