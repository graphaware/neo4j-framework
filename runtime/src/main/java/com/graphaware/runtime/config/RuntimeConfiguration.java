package com.graphaware.runtime.config;

import com.graphaware.common.util.Pair;
import org.apache.commons.configuration2.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Reader for GraphAware Framework configuration.
 */
public interface RuntimeConfiguration {

    boolean runtimeEnabled();

    Map<String, ModuleConfig> loadConfig();
}
