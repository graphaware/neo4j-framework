package com.graphaware.runtime.config;

import com.graphaware.common.strategy.InclusionStrategies;

/**
 * Encapsulates all configuration of a single {@link com.graphaware.runtime.GraphAwareRuntimeModule}. Modules that need
 * no configuration should use {@link NullRuntimeModuleConfiguration}.
 */
public interface RuntimeModuleConfiguration {

    /**
     * Get the inclusion strategies used by this module. If unsure, return {@link com.graphaware.common.strategy.InclusionStrategies#all()},
     * which includes all non-internal nodes, properties, and relationships.
     *
     * @return strategy.
     */
    InclusionStrategies getInclusionStrategies();
}
