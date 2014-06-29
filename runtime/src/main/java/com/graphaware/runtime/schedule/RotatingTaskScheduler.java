package com.graphaware.runtime.schedule;

import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.timer.TimingStrategy;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class RotatingTaskScheduler implements TaskScheduler {
    private static final Logger LOG = Logger.getLogger(RotatingTaskScheduler.class);

    private final GraphDatabaseService database;
    private final ModuleMetadataRepository repository;
    private final TimingStrategy timingStrategy;

    //todo: these two should be made concurrent if we use more than 1 thread for the background work
    private final Map<TimerDrivenModule, TimerDrivenModuleMetadata> moduleMetadata = new LinkedHashMap<>();
    private Iterator<Map.Entry<TimerDrivenModule, TimerDrivenModuleMetadata>> moduleMetadataIterator;

    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    public RotatingTaskScheduler(GraphDatabaseService database, ModuleMetadataRepository repository, TimingStrategy timingStrategy) {
        this.database = database;
        this.repository = repository;
        this.timingStrategy = timingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <M extends TimerDrivenModuleMetadata, T extends TimerDrivenModule<M>> void registerMetadata(T module, M metadata) {
        if (moduleMetadataIterator != null) {
            throw new IllegalStateException("Task scheduler can not accept metadata after it has been started. This is a bug.");
        }

        moduleMetadata.put(module, metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (moduleMetadata.isEmpty()) {
            LOG.info("There are no timer-driven runtime modules. Not scheduling any tasks.");
            return;
        }

        LOG.info("There are " + moduleMetadata.size() + " timer-driven runtime modules. Starting timer...");
        scheduleNextTask(-1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        worker.shutdown();
        try {
            worker.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn(this.getClass().getName() + " did not properly shut down");
        }
    }

    /**
     * Schedule next task.
     *
     * @param lastTaskDuration duration of the last task in nanoseconds.
     */
    private void scheduleNextTask(long lastTaskDuration) {
        long delay = timingStrategy.nextDelay(lastTaskDuration);
        LOG.debug("Scheduling next task with a delay of " + delay + " ms.");
        worker.schedule(nextTask(), delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Create next task wrapped in a {@link Runnable}. The {@link Runnable} schedules the next task when finished.
     *
     * @return next task to be run wrapped in a {@link Runnable}.
     */
    protected Runnable nextTask() {
        return new Runnable() {
            @Override
            public void run() {
                long totalTime = -1;
                try {
                    LOG.debug("Running a scheduled task...");
                    long startTime = System.nanoTime();

                    runNextTask();

                    totalTime = (System.nanoTime() - startTime);
                    LOG.debug("Successfully completed scheduled task in " + totalTime / 1000 + " microseconds");
                } catch (Exception e) {
                    LOG.warn("Task execution threw an exception: " + e.getMessage(), e);
                } finally {
                    scheduleNextTask(totalTime);
                }
            }
        };
    }

    /**
     * Run the next task.
     *
     * @param <M> metadata type of the metadata passed into the module below.
     * @param <T> module type of the module that will be delegated to.
     */
    private <M extends TimerDrivenModuleMetadata, T extends TimerDrivenModule<M>> void runNextTask() {
        Map.Entry<T, M> moduleAndMetadata = nextModuleAndMetadata();
        T module = moduleAndMetadata.getKey();
        M metadata = moduleAndMetadata.getValue();

        try (Transaction tx = database.beginTx()) {
            M newMetadata = module.doSomeWork(metadata, database);
            repository.persistModuleMetadata(module, newMetadata);
            moduleMetadata.put(module, newMetadata);
            tx.success();
        }
    }

    /**
     * Find the next module to be delegated to and its metadata.
     *
     * @param <M> metadata type.
     * @param <T> module type.
     * @return module & metadata as a {@link Map.Entry}
     */
    private <M extends TimerDrivenModuleMetadata, T extends TimerDrivenModule<M>> Map.Entry<T, M> nextModuleAndMetadata() {
        if (moduleMetadataIterator == null || !moduleMetadataIterator.hasNext()) {
            moduleMetadataIterator = moduleMetadata.entrySet().iterator();
        }

        //this class controls the insertion to the map and it is type-safe.
        //noinspection unchecked
        return (Map.Entry<T, M>) moduleMetadataIterator.next();
    }
}
