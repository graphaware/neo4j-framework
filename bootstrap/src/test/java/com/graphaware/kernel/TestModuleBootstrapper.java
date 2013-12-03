package com.graphaware.kernel;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper;
import org.neo4j.kernel.configuration.Config;

/**
 * {@link com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper} for {@link TestRuntimeModule}.
 */
public class TestModuleBootstrapper implements GraphAwareRuntimeModuleBootstrapper {

    @Override
    public void bootstrap(GraphAwareRuntime framework, Config config) {
        framework.registerModule(new TestRuntimeModule());
    }
}
