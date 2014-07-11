package com.graphaware.runtime.schedule;

import com.graphaware.common.util.Pair;
import com.graphaware.runtime.metadata.DefaultTimerDrivenModuleMetadata;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.TimerDrivenModuleContext;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import com.graphaware.runtime.module.TimerDrivenModule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link TaskScheduler} that delegates to the registered {@link TimerDrivenModule}s in round-robin fashion, in the order
 * in which the modules were registered. All work performed by this implementation is done by a single thread.
 */
public class RotatingTaskScheduler implements TaskScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(RotatingTaskScheduler.class);

    private final GraphDatabaseService database;
    private final ModuleMetadataRepository repository;
    private final TimingStrategy timingStrategy;

    //todo: these two should be made concurrent if we use more than 1 thread for the background work
    private final Map<TimerDrivenModule, TimerDrivenModuleContext> moduleContexts = new LinkedHashMap<>();
    private Iterator<Map.Entry<TimerDrivenModule, TimerDrivenModuleContext>> moduleContextIterator;

    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    /**
     * Construct a new task scheduler.
     *
     * @param database       against which the modules are running.
     * @param repository     for persisting metadata.
     * @param timingStrategy strategy for timing the work delegation.
     */
    public RotatingTaskScheduler(GraphDatabaseService database, ModuleMetadataRepository repository, TimingStrategy timingStrategy) {
        this.database = database;
        this.repository = repository;
        this.timingStrategy = timingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C extends TimerDrivenModuleContext, T extends TimerDrivenModule<C>> void registerModuleAndContext(T module, C context) {
        if (moduleContextIterator != null) {
            throw new IllegalStateException("Task scheduler can not accept modules after it has been started. This is a bug.");
        }

        LOG.info("Registering module " + module.getId() + " and its context with the task scheduler.");
        moduleContexts.put(module, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (moduleContexts.isEmpty()) {
            LOG.info("There are no timer-driven runtime modules. Not scheduling any tasks.");
            return;
        }

        LOG.info("There are " + moduleContexts.size() + " timer-driven runtime modules. Scheduling the first task...");
        scheduleNextTask(-2); //-2 here is to indicate initial task, a bit of a hack for now to allow longer delay for the very first timer firing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        LOG.info("Terminating task scheduler...");
        worker.shutdownNow();
        LOG.info("Task scheduler terminated successfully.");
    }

    /**
     * Schedule next task.
     *
     * @param lastTaskDuration duration of the last task in nanoseconds, negative if unknown.
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
     * @param <C> type of the context passed into the module below.
     * @param <T> module type of the module that will be delegated to.
     */
    private <C extends TimerDrivenModuleContext, T extends TimerDrivenModule<C>> void runNextTask() {
        Pair<T, C> moduleAndContext = findNextModuleAndContext();

        if (moduleAndContext == null) {
            return; //no module withes to run
        }

        T module = moduleAndContext.first();
        C context = moduleAndContext.second();

        try (Transaction tx = database.beginTx()) {
            C newContext = module.doSomeWork(context, database);
            repository.persistModuleMetadata(module, new DefaultTimerDrivenModuleMetadata(newContext));
            moduleContexts.put(module, newContext);
            tx.success();
        }
    }

    /**
     * Find the next module that is ready to be delegated to, and its context.
     *
     * @param <C> context type.
     * @param <T> module type.
     * @return module & context.
     */
    private <C extends TimerDrivenModuleContext, T extends TimerDrivenModule<C>> Pair<T, C> findNextModuleAndContext() {
        int totalModules = moduleContexts.size();
        long now = System.currentTimeMillis();

        for (int i = 0; i < totalModules; i++) {
            Pair<T, C> candidate = nextModuleAndContext();
            if (candidate.second() == null || candidate.second().earliestNextCall() <= now) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Find the next module whose turn it would be and its context.
     *
     * @param <C> context type.
     * @param <T> module type.
     * @return module & context.
     */
    private <C extends TimerDrivenModuleContext, T extends TimerDrivenModule<C>> Pair<T, C> nextModuleAndContext() {
        if (moduleContextIterator == null || !moduleContextIterator.hasNext()) {
            moduleContextIterator = moduleContexts.entrySet().iterator();
        }

        Map.Entry<TimerDrivenModule, TimerDrivenModuleContext> entry = moduleContextIterator.next();

        //noinspection unchecked
        return new Pair<>((T) entry.getKey(), (C) entry.getValue());
    }
}
