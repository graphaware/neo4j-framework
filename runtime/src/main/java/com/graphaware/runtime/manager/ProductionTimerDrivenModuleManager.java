package com.graphaware.runtime.manager;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.schedule.RotatingTaskScheduler;
import com.graphaware.runtime.schedule.TaskScheduler;
import com.graphaware.runtime.timer.FixedDelayTimingStrategy;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Collections;
import java.util.Set;

/**
 *
 */
public class ProductionTimerDrivenModuleManager extends BaseModuleManager<TimerDrivenModuleMetadata, TimerDrivenModule<? extends TimerDrivenModuleMetadata<?>>> implements TimerDrivenModuleManager {

    private final GraphDatabaseService database;
    private final TaskScheduler taskScheduler;

    public ProductionTimerDrivenModuleManager(ModuleMetadataRepository metadataRepository, GraphDatabaseService database) {
        super(metadataRepository);
        this.database = database;
        this.taskScheduler = new RotatingTaskScheduler(database, metadataRepository, Collections.<TimerDrivenModule>emptyList(), new FixedDelayTimingStrategy(1000));
    }

    @Override
    protected void handleCorruptMetadata(TimerDrivenModule module) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void handleNoMetadata(TimerDrivenModule module) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected TimerDrivenModuleMetadata createFreshMetadata(TimerDrivenModule module) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected TimerDrivenModuleMetadata acknowledgeMetadata(TimerDrivenModule module, TimerDrivenModuleMetadata metadata) {
        return metadata;
    }

    @Override
    public void startModules() {
        // We will start the scheduling here.
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
        taskScheduler.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownModules() {
        super.shutdownModules();
        taskScheduler.stop();
    }
}
