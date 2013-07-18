package com.graphaware.neo4j.framework.config;

/**
 * Default {@link FrameworkConfiguration} for {@link com.graphaware.neo4j.framework.GraphAwareFramework}. Singleton.
 */
public final class DefaultFrameworkConfiguration extends BaseFrameworkConfiguration {

    public static final DefaultFrameworkConfiguration INSTANCE = new DefaultFrameworkConfiguration();

    public static DefaultFrameworkConfiguration getInstance() {
        return INSTANCE;
    }

    private DefaultFrameworkConfiguration() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String separator() {
        return DEFAULT_SEPARATOR;
    }
}
