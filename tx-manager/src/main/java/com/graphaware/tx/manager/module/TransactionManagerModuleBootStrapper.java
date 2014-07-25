package com.graphaware.tx.manager.module;

import com.graphaware.runtime.module.RuntimeModule;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Map;

public class TransactionManagerModuleBootStrapper implements RuntimeModuleBootstrapper {

    @Override
    public RuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        return new TransactionManagerModule(moduleId, database);
    }
}
