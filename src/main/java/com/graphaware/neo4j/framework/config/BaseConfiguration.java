package com.graphaware.neo4j.framework.config;

/**
 * Abstract base-class for {@link FrameworkConfiguration} implementations.
 */
public abstract class BaseConfiguration implements FrameworkConfiguration {

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPrefix(String id) {
        return GA_PREFIX + id + "_";
    }
}
