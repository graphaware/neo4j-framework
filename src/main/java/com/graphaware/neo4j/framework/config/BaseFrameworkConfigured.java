package com.graphaware.neo4j.framework.config;

/**
 * Convenience abstract base-class for {@link FrameworkConfigured} components.
 */
public abstract class BaseFrameworkConfigured implements FrameworkConfigured {

    private FrameworkConfiguration configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void configurationChanged(FrameworkConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Get the current configuration of the component.
     *
     * @return current configuration.
     * @throws IllegalStateException if it hasn't been configured yet.
     */
    protected FrameworkConfiguration getConfig() {
        if (configuration == null) {
            throw new IllegalStateException("Module hasn't been configured. Has it been registered with the GraphAware framework?");
        }

        return configuration;
    }
}
