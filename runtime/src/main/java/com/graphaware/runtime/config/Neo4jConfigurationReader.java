package com.graphaware.runtime.config;

import com.graphaware.runtime.settings.FrameworkSettingsDeclaration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.neo4j.configuration.Config;

/**
 * {@link ConfigurationReader} that reads the configuration from a file in the `conf` directory of Neo4j. The name of the
 * file is determined by the {@link FrameworkSettingsDeclaration#ga_config_file_name} setting in `neo4j.conf` and defaults
 * to `graphaware.conf`.
 */
public class Neo4jConfigurationReader implements ConfigurationReader {

    private final Config neo4jConfig;

    public Neo4jConfigurationReader(Config neo4jConfig) {
        this.neo4jConfig = neo4jConfig;
    }

    @Override
    public Configuration readConfiguration() {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(new Parameters()
                                .fileBased()
                                .setURL(getClass().getClassLoader().getResource(neo4jConfig.get(FrameworkSettingsDeclaration.ga_config_file_name))));

        CompositeConfiguration cc = new CompositeConfiguration();

        try {
            cc.addConfiguration(builder.getConfiguration());
            cc.addConfiguration(new SystemConfiguration()); //for testing
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

        return cc;
    }
}
