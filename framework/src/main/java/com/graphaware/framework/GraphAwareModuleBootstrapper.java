package com.graphaware.framework;

import org.neo4j.kernel.configuration.Config;

/**
 * Component that automatically bootstraps a {@link GraphAwareModule} based on config parameters passed to Neo4j.
 */
public interface GraphAwareModuleBootstrapper {

    /**
     * Bootstrap a module.
     *
     * @param framework GraphAware framework.
     * @param config    passed to Neo4j.
     */
    void bootstrap(GraphAwareFramework framework, Config config);
}
