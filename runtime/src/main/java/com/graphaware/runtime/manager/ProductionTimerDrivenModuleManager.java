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
 * Production implementation of {@link TimerDrivenModuleManager}. Must be backed by a {@link GraphDatabaseService},
 * as there is no support for using {@link TimerDrivenModule}s in batch mode (i.e. with {@link org.neo4j.unsafe.batchinsert.BatchInserter}s).
 */
public class ProductionTimerDrivenModuleManager extends BaseModuleManager<TimerDrivenModuleMetadata, TimerDrivenModule<? extends TimerDrivenModuleMetadata>> implements TimerDrivenModuleManager<TimerDrivenModule<? extends TimerDrivenModuleMetadata>> {

    private final GraphDatabaseService database;
    private final TaskScheduler taskScheduler;

    /**
     * Construct a new manager.
     *
     * @param database           storing graph data.
     * @param metadataRepository for storing module metadata.
     */
    public ProductionTimerDrivenModuleManager(GraphDatabaseService database, ModuleMetadataRepository metadataRepository) {
        super(metadataRepository);
        this.database = database;

        taskScheduler = new RotatingTaskScheduler(database, metadataRepository, new FixedDelayTimingStrategy(200));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerDrivenModuleMetadata createFreshMetadata(TimerDrivenModule module) {
        return module.createFirstMetadata(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerDrivenModuleMetadata acknowledgeMetadata(TimerDrivenModule module, TimerDrivenModuleMetadata metadata) {
        taskScheduler.registerMetadata(module, metadata);
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startModules() {
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
