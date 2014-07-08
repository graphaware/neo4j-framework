package com.graphaware.generator;

import com.graphaware.generator.config.GeneratorConfiguration;

/**
 * Component that generates a graph based on {@link GeneratorConfiguration}.
 */
public interface GraphGenerator {

    /**
     * Generate a graph.
     *
     * @param configuration for the graph generation.
     */
    void generateGraph(GeneratorConfiguration configuration);
}
