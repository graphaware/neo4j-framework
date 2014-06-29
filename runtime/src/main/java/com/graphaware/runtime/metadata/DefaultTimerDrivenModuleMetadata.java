package com.graphaware.runtime.metadata;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;

/**
 * Default production implementation of {@link com.graphaware.runtime.metadata.TimerDrivenModuleMetadata}.
 */
public class DefaultTimerDrivenModuleMetadata implements TimerDrivenModuleMetadata {

    private final TimerDrivenModuleContext configuration;

    /**
     * Construct new metadata.
     *
     * @param configuration module configuration held by the metadata.
     */
    public DefaultTimerDrivenModuleMetadata(TimerDrivenModuleContext configuration) {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleContext<?> getLastContext() {
        return configuration;
    }
}
