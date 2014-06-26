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
    	// modules of this type don't even have an initialise method so I've no idea what to do here
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void startModules() {
    	// Given that there's no apparent initialisation, we can probably start the scheduling here.
    	// Also note that this isn't getting called at present
        throw new UnsupportedOperationException("not yet implemented");
        /*
         * I reckon we want to have something here that says "start the scheduler" and then it's off and running.
         *
         * The objects that are actually scheduled need to provide the environment in which the modules will operate, namely:
         * - start and manage a database transaction
         *   (although our diagram said the tx boundary was just below GraphAwareRuntime, that was only for the init process)
         * - ensure that exceptions thrown from the modules don't affect the module manager adversely
         * - ensure persistence of state of module and/or graph walk
         *   (i.e., make sure that if the database is shut down then we can reinitialise)
         */
    }

}
