package com.graphaware.runtime;

import java.util.Map;

/**
 * Component that automatically bootstraps a {@link GraphAwareRuntimeModule} based on config parameters passed to Neo4j.
 * <p/>
 * Implementations can expect that if there is the following entry in neo4j.properties
 * <p/>
 * com.graphaware.module.x.y = z
 * <p/>
 * where x is the ID of the module, y is the order in which the module will be registered with respect to other modules,
 * and z is the fully qualified class name of the bootstrapper implementation, then x will be passed to the {@link #bootstrap(GraphAwareRuntime, String, java.util.Map)}
 * method of an instance of z as the second parameter (moduleId). Moreover, from all other entries of the form
 * <p/>
 * com.graphaware.module.x.a = b
 * <p/>
 * a map with a's as keys and b's as values will be passed as the third parameter (config) to the {@link #bootstrap(GraphAwareRuntime, String, java.util.Map)}
 * method. {@link GraphAwareRuntimeModuleBootstrapper} implementations should document, which key-value configurations
 * they expect.
 *
 * @see com.graphaware.runtime.bootstrap.RuntimeKernelExtension
 */
public interface GraphAwareRuntimeModuleBootstrapper {

    /**
     * Bootstrap a module.
     *
     * @param runtime  GraphAware Runtime.
     * @param moduleId ID of the module.
     * @param config   for this module as key-value pairs.
     */
    void bootstrap(GraphAwareRuntime runtime, String moduleId, Map<String, String> config);
}
