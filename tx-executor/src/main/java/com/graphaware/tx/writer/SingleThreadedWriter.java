package com.graphaware.tx.writer;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.callable;

/**
 * A {@link DatabaseWriter} that maintains a queue of tasks and writes to the database in a single thread by constantly
 * pulling the tasks from the head of the queue in a single thread.
 * <p/>
 * If the queue capacity is full, tasks are dropped and a warning is logged.
 */
public abstract class SingleThreadedWriter extends AbstractScheduledService implements DatabaseWriter {

    private static final Logger LOG = LoggerFactory.getLogger(SingleThreadedWriter.class);

    protected final LinkedBlockingQueue<RunnableFuture<?>> queue;

    private volatile long lastLoggedTime = System.currentTimeMillis();

    protected final GraphDatabaseService database;

    /**
     * Construct a new writer with a default queue capacity of 10,000.
     *
     * @param database to write to.
     */
    protected SingleThreadedWriter(GraphDatabaseService database) {
        this(database, 10000);
    }

    /**
     * Construct a new writer with.
     *
     * @param database      to write to.
     * @param queueCapacity capacity of the queue.
     */
    protected SingleThreadedWriter(GraphDatabaseService database, int queueCapacity) {
        this.database = database;
        queue = new LinkedBlockingQueue<>(queueCapacity);
    }

    /**
     * Start the processing of tasks.
     */
    @PostConstruct
    public void start() {
        startAsync();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Runnable task) {
        write(task, "UNKNOWN");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Runnable task, String id) {
        write(callable(task), id, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T write(final Callable<T> task, String id, int waitMillis) {
        RunnableFuture<T> futureTask = createTask(task);

        if (!queue.offer(futureTask)) {
            LOG.warn("Could not write task to queue as it is too full. We're losing writes now.");
            return null;
        }

        if (waitMillis <= 0) {
            //no need to wait, caller not interested in result
            return null;
        }

        return block(futureTask, id, waitMillis);
    }

    /**
     * Create a runnable future from the given task.
     *
     * @param task task.
     * @return future.
     */
    protected abstract <T> RunnableFuture<T> createTask(final Callable<T> task);

    private <T> T block(RunnableFuture<T> futureTask, String id, int waitMillis) {
        try {
            return futureTask.get(waitMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Waiting for execution of a task was interrupted. ID: " + id, e);
        } catch (ExecutionException e) {
            LOG.warn("Execution of a task threw an exception. ID: " + id, e);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (TimeoutException e) {
            LOG.warn("Task didn't get executed within " + waitMillis + "ms. ID: " + id);
        }

        return null;
    }

    protected void logQueueSizeIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastLoggedTime > 5000 && queue.size() > 0) {
            LOG.info("Queue size: " + queue.size());
            lastLoggedTime = now;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.MILLISECONDS);
    }
}
