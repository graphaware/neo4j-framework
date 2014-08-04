package com.graphaware.runtime.config;

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.runtime.strategy.InclusionStrategiesFactory;

/**
 * {@link TxDrivenModuleConfiguration} with fluent interface.
 * Intended for users of Neo4j in embedded mode to programatically configure the runtime.
 */
public class FluentTxDrivenModuleConfiguration extends BaseTxDrivenModuleConfiguration<FluentTxDrivenModuleConfiguration> {

    /**
     * Creates an instance with default values, i.e., with {@link com.graphaware.runtime.strategy.InclusionStrategiesFactory#allBusiness()}.
     *
     * @return The {@link FluentRuntimeConfiguration} instance.
     */
    public static FluentTxDrivenModuleConfiguration defaultConfiguration() {
        return new FluentTxDrivenModuleConfiguration();
    }

    /**
     * Create a new configuration with {@link com.graphaware.runtime.strategy.InclusionStrategiesFactory#allBusiness()}.
     */
    private FluentTxDrivenModuleConfiguration() {
        super(InclusionStrategiesFactory.allBusiness());
    }

    /**
     * Create a new configuration.
     *
     * @param inclusionStrategies of the configuration.
     */
    private FluentTxDrivenModuleConfiguration(InclusionStrategies inclusionStrategies) {
        super(inclusionStrategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FluentTxDrivenModuleConfiguration newInstance(InclusionStrategies inclusionStrategies) {
        return new FluentTxDrivenModuleConfiguration(inclusionStrategies);
    }
}
