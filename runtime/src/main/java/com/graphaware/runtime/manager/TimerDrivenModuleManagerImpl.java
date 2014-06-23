package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TimerDrivenRuntimeModule;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 */
public class TimerDrivenModuleManagerImpl extends BaseModuleManager<TimerDrivenRuntimeModule> implements TimerDrivenModuleManager {

    private final GraphDatabaseService database;

    public TimerDrivenModuleManagerImpl(ModuleMetadataRepository metadataRepository, GraphDatabaseService database) {
        super(metadataRepository);
        this.database = database;
    }

    @Override
    protected void doInitialize(TimerDrivenRuntimeModule module) {
        module.initialize(database);
    }

    @Override
    protected void doReinitialize(TimerDrivenRuntimeModule module) {
        module.reinitialize(database);
    }
}
