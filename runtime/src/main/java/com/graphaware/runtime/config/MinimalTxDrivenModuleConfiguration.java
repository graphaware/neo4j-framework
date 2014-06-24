package com.graphaware.runtime.config;

import com.graphaware.common.strategy.InclusionStrategies;

/**
 * Minimal {@link TxDrivenModuleConfiguration} that can be instantiated and configured further using fluent API.
 */
public class MinimalTxDrivenModuleConfiguration extends BaseTxDrivenModuleConfiguration<MinimalTxDrivenModuleConfiguration> implements TxDrivenModuleConfiguration {

    public MinimalTxDrivenModuleConfiguration() {
        super(InclusionStrategies.all());
    }

    public MinimalTxDrivenModuleConfiguration(InclusionStrategies inclusionStrategies) {
        super(inclusionStrategies);
    }

    @Override
    protected MinimalTxDrivenModuleConfiguration newInstance(InclusionStrategies inclusionStrategies) {
        return new MinimalTxDrivenModuleConfiguration(inclusionStrategies);
    }
}
