package com.graphaware.neo4j.framework.config;

/**
 * Component configurable by {@link com.graphaware.neo4j.framework.GraphAwareFramework}.
 */
public interface FrameworkConfigured {

    /**
     * Acknowledge a configuration change.
     *
     * @param configuration new configuration.
     */
    void configurationChanged(FrameworkConfiguration configuration);
}
