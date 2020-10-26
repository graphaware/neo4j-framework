package com.graphaware.runtime.config;

import com.graphaware.runtime.module.Module;

import java.util.Map;

/**
 * Reader for GraphAware Framework configuration.
 */
public interface RuntimeConfiguration {

    /**
     * Is runtime enabled for the database this configuration is for?
     *
     * @return true iff enabled.
     */
    boolean runtimeEnabled();

    /**
     * Load the runtime configuration.
     *
     * @return a configuration map, where key is a {@link Module} ID and value is the module's {@link ModuleConfig}.
     */
    Map<String, ModuleConfig> loadConfig();
}
