package com.graphaware.runtime.config;

import org.apache.commons.configuration2.Configuration;

/**
 * Component that knows where and how to find the {@link RuntimeConfiguration}.
 */
public interface ConfigurationReader {

    /**
     * @return Runtime {@link Configuration}.
     */
    Configuration readConfiguration();
}
