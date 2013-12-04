package com.graphaware.runtime;

import org.neo4j.graphdb.config.Setting;
import org.neo4j.kernel.configuration.Config;

/**
 * Component that automatically bootstraps a {@link GraphAwareRuntimeModule} based on config parameters passed to Neo4j.
 */
public interface GraphAwareRuntimeModuleBootstrapper {

    /**
     * Bootstrap a module.
     *
     * @param framework GraphAware framework.
     * @param config    passed to Neo4j.
     */
    void bootstrap(GraphAwareRuntime framework, Config config);
}
