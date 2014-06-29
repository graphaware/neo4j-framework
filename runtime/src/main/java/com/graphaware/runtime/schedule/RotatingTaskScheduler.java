package com.graphaware.runtime.schedule;

import com.graphaware.runtime.metadata.GraphPosition;
import com.graphaware.runtime.metadata.ModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.timer.FixedDelayTimingStrategy;
import com.graphaware.runtime.timer.TimingStrategy;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class RotatingTaskScheduler implements TaskScheduler {
    private static final Logger LOG = Logger.getLogger(RotatingTaskScheduler.class);

    private final GraphDatabaseService database;
    private final ModuleMetadataRepository repository;
    private final List<TimerDrivenModule> modules;
    private final TimingStrategy timingStrategy;

    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger lastModule = new AtomicInteger(-1);
    private final Map<TimerDrivenModule, ModuleMetadata> moduleMetadata = new HashMap<>();

    public RotatingTaskScheduler(GraphDatabaseService database, ModuleMetadataRepository repository, List<TimerDrivenModule> modules, TimingStrategy timingStrategy) {
        this.database = database;
        this.repository = repository;
        this.modules = modules;
        this.timingStrategy = timingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (modules.isEmpty()) {
            LOG.info("There are no timer-driven runtime modules. Not scheduling any tasks.");
            return;
        }

        LOG.info("There are " + modules.size() + " timer-driven runtime modules. Starting timer...");
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

    private void scheduleNextTask(long lastTaskDuration) {
        long delay = timingStrategy.nextDelay(lastTaskDuration);
        LOG.debug("Scheduling next task with a delay of " + delay + " ms.");
        worker.schedule(nextTask(), delay, TimeUnit.MILLISECONDS);
    }

    protected Runnable nextTask() {
        return new Runnable() {
            @Override
            public void run() {
                long totalTime = -1;
                try {
                    LOG.debug("Running a scheduled task...");
                    long startTime = System.nanoTime();

                    runNextTask();

                    totalTime = (System.nanoTime() - startTime) / 1000;
                    LOG.debug("Successfully completed scheduled task in " + totalTime + " microseconds");
                } catch (Exception e) {
                    LOG.warn("Task execution threw an exception: " + e.getMessage(), e);
                } finally {
                    scheduleNextTask(totalTime);
                }
            }
        };
    }

    private <M extends TimerDrivenModuleMetadata<?>> void runNextTask() {
        int nextModuleIndex = lastModule.incrementAndGet() % modules.size();
        TimerDrivenModule<M> module = modules.get(nextModuleIndex);

        try (Transaction tx = database.beginTx()) {
            M metadata = getMetadata(module);
            M newMetadata = module.doSomeWork(metadata, database);
            persistMetadata(module, newMetadata);
            moduleMetadata.put(module, newMetadata);

            tx.success();
        }
    }

    private <M extends TimerDrivenModuleMetadata<?>> M getMetadata(TimerDrivenModule<M> module) {
        if (!moduleMetadata.containsKey(module)) {
            moduleMetadata.put(module, loadMetadata(module));
        }

        return (M) moduleMetadata.get(module);
    }

    private <M extends TimerDrivenModuleMetadata<?>> M loadMetadata(TimerDrivenModule<M> module) {
        return repository.getModuleMetadata(module);
    }

    private <M extends TimerDrivenModuleMetadata<?>> void persistMetadata(TimerDrivenModule<M> module, M metadata) {
        repository.persistModuleMetadata(module, metadata);
    }
}
