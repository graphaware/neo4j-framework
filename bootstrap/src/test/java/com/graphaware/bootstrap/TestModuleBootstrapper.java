package com.graphaware.bootstrap;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.helpers.Settings;
import org.neo4j.kernel.configuration.Config;

import static org.neo4j.helpers.Settings.*;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper} for {@link TestRuntimeModule}.
 */
public class TestModuleBootstrapper implements GraphAwareRuntimeModuleBootstrapper {

    public static final Setting<String> MODULE_ENABLED = setting("com.graphaware.module.test.enabled", STRING, TestModuleBootstrapper.class.getCanonicalName());

    @Override
    public void bootstrap(GraphAwareRuntime runtime, Config config) {
        runtime.registerModule(new TestRuntimeModule());
    }
}
