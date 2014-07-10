package com.graphaware.tx.manager.module;

import com.graphaware.runtime.GraphAwareRuntimeModule;
import com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

public class TransactionManagerModuleBootStrapper implements GraphAwareRuntimeModuleBootstrapper {
    @Override
    public GraphAwareRuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        return new TransactionManagerModule(moduleId, database);
    }
}
