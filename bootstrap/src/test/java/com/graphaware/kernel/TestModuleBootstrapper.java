package com.graphaware.kernel;

import com.graphaware.framework.GraphAwareFramework;
import com.graphaware.framework.GraphAwareModuleBootstrapper;
import org.neo4j.kernel.configuration.Config;

/**
 * {@link GraphAwareModuleBootstrapper} for {@link TestModule}.
 */
public class TestModuleBootstrapper implements GraphAwareModuleBootstrapper {

    @Override
    public void bootstrap(GraphAwareFramework framework, Config config) {
        framework.registerModule(new TestModule());
    }
}
