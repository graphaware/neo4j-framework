package com.graphaware.neo4j.framework.config;

/**
 * Default {@link FrameworkConfiguration} for {@link com.graphaware.neo4j.framework.GraphAwareFramework}. Singleton.
 */
public final class DefaultConfiguration extends BaseConfiguration {

    public static final DefaultConfiguration INSTANCE = new DefaultConfiguration();

    public static DefaultConfiguration getInstance() {
        return INSTANCE;
    }

    private DefaultConfiguration() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String separator() {
        return DEFAULT_SEPARATOR;
    }
}
