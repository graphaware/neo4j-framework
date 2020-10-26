package com.graphaware.runtime.settings;

import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.configuration.Description;
import org.neo4j.configuration.SettingsDeclaration;
import org.neo4j.graphdb.config.Setting;

import static org.neo4j.configuration.SettingImpl.newBuilder;
import static org.neo4j.configuration.SettingValueParsers.BOOL;
import static org.neo4j.configuration.SettingValueParsers.STRING;

@ServiceProvider
public class FrameworkSettingsDeclaration implements SettingsDeclaration {

    @Description("Name of GraphAware Config File")
    public static final Setting<String> ga_config_file_name = newBuilder("com.graphaware.config.file", STRING, "graphaware.conf").build();
}
