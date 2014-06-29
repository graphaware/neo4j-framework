package com.graphaware.runtime.module;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

/**
 * Component that automatically bootstraps a {@link RuntimeModule} based on config parameters passed to Neo4j.
 * <p/>
 * Implementations can expect that if there is the following entry in neo4j.properties
 * <p/>
 * com.graphaware.module.x.y = z
 * <p/>
 * where x is the ID of the module, y is the order in which the module will be registered with respect to other modules,
 * and z is the fully qualified class name of the bootstrapper implementation, then x will be passed to the {@link #bootstrapModule(String, java.util.Map, org.neo4j.graphdb.GraphDatabaseService)}
 * method of an instance of z as the first parameter (moduleId). Moreover, from all other entries of the form
 * <p/>
 * com.graphaware.module.x.a = b
 * <p/>
 * a map with a's as keys and b's as values will be passed as the second parameter (config) to the {@link #bootstrapModule(String, java.util.Map, org.neo4j.graphdb.GraphDatabaseService)}
 * method. {@link RuntimeModuleBootstrapper} implementations should document, which key-value configurations
 * they expect.
 *
 * @see com.graphaware.runtime.bootstrap.RuntimeKernelExtension
 */
public interface RuntimeModuleBootstrapper {

    /**
     * Create a new instance of a module.
     *
     * @param moduleId ID of the module.
     * @param config   for this module as key-value pairs.
     * @param database which the module will run on.
     */
    RuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database);
}
