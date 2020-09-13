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

    @Description("Enable GraphAware Runtime")
    public static final Setting<Boolean> ga_runtime_enabled = newBuilder("com.graphaware.runtime.enabled", BOOL, false).build();

    @Description("GraphAware Modules Configuration")
    public static final Setting<String> ga_modules_config = newBuilder("com.graphaware.modules.config", STRING, "").build();
}
