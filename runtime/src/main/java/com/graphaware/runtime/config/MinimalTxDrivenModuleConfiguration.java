package com.graphaware.runtime.config;

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.strategy.InclusionStrategiesFactory;

/**
 * Minimal {@link TxDrivenModuleConfiguration} that can be instantiated and configured further using fluent API.
 */
public class MinimalTxDrivenModuleConfiguration extends BaseTxDrivenModuleConfiguration<MinimalTxDrivenModuleConfiguration> {

    /**
     * Create a new configuration with {@link com.graphaware.runtime.strategy.InclusionStrategiesFactory#allBusiness()}.
     */
    public MinimalTxDrivenModuleConfiguration() {
        super(InclusionStrategiesFactory.allBusiness());
    }

    /**
     * Create a new configuration.
     *
     * @param inclusionStrategies of the configuration.
     */
    public MinimalTxDrivenModuleConfiguration(InclusionStrategies inclusionStrategies) {
        super(inclusionStrategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MinimalTxDrivenModuleConfiguration newInstance(InclusionStrategies inclusionStrategies) {
        return new MinimalTxDrivenModuleConfiguration(inclusionStrategies);
    }
}
