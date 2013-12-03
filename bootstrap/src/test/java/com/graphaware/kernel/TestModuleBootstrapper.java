package com.graphaware.kernel;

import com.graphaware.framework.GraphAwareRuntime;
import com.graphaware.framework.GraphAwareRuntimeModuleBootstrapper;
import org.neo4j.kernel.configuration.Config;

/**
 * {@link com.graphaware.framework.GraphAwareRuntimeModuleBootstrapper} for {@link TestRuntimeModule}.
 */
public class TestModuleBootstrapper implements GraphAwareRuntimeModuleBootstrapper {

    @Override
    public void bootstrap(GraphAwareRuntime framework, Config config) {
        framework.registerModule(new TestRuntimeModule());
    }
}
