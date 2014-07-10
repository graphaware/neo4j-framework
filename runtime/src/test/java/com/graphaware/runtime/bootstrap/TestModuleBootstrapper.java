package com.graphaware.runtime.bootstrap;

import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;

import java.util.Map;

import static org.neo4j.helpers.Settings.STRING;
import static org.neo4j.helpers.Settings.setting;

/**
 * {@link com.graphaware.runtime.module.RuntimeModuleBootstrapper} for {@link TestRuntimeModule}.
 */
public class TestModuleBootstrapper implements RuntimeModuleBootstrapper {

    public static final Setting<String> MODULE_ENABLED = setting("com.graphaware.module.test.1", STRING, TestModuleBootstrapper.class.getCanonicalName());
    public static final Setting<String> MODULE_CONFIG = setting("com.graphaware.module.test.configKey", STRING, "configValue");

    @Override
    public TxDrivenModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        return new TestRuntimeModule(moduleId, config);
    }
}
