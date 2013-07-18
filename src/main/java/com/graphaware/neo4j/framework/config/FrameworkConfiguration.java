package com.graphaware.neo4j.framework.config;

/**
 * {@link com.graphaware.neo4j.framework.GraphAwareFramework} configuration.
 */
public interface FrameworkConfiguration {

    /**
     * Prefix for GraphAware internal nodes, relationships, and properties. This is fixed as there is little chance
     * that users would have a reason to change it.
     */
    public static final String GA_PREFIX = "_GA_";

    /**
     * Key of a property present on GraphAware internal nodes. This is fixed as there is little chance
     * that users would have a reason to change it.
     */
    public static final String GA_NODE_PROPERTY_KEY = GA_PREFIX + "LABEL";

    /**
     * Default value for {@link #separator()}.
     */
    static final String DEFAULT_SEPARATOR = "#";

    /**
     * Get the separator used as an information delimiter for String-convertible nodes, relationships, and properties.
     *
     * @return separator.
     */
    String separator();

    /**
     * Create prefix a component should use for internal data it reads/writes (nodes, relationships, properties).
     * @param id of the component/module.
     * @return prefix of the form {@link #GA_PREFIX} + id + "_"
     */
    String createPrefix(String id);
}
