package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.module.TimerDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 */
public class TimerDrivenModuleManagerImpl extends BaseModuleManager<TimerDrivenModule> implements TimerDrivenModuleManager {

    private final GraphDatabaseService database;

    public TimerDrivenModuleManagerImpl(ModuleMetadataRepository metadataRepository, GraphDatabaseService database) {
        super(metadataRepository);
        this.database = database;
    }

    @Override
    protected void initializeModule2(TimerDrivenModule module) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void startModules() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
