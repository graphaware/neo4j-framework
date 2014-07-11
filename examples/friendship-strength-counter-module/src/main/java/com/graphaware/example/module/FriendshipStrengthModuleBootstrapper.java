package com.graphaware.example.module;

import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import com.graphaware.runtime.module.TxDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

/**
 * {@link com.graphaware.runtime.module.RuntimeModuleBootstrapper} for {@link FriendshipStrengthModule}.
 */
public class FriendshipStrengthModuleBootstrapper implements RuntimeModuleBootstrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        return new FriendshipStrengthModule(moduleId, database);
    }
}
