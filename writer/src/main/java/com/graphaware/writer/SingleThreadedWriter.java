/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.writer;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.callable;

/**
 * A {@link DatabaseWriter} that maintains a queue of tasks and writes to the database in a single thread by constantly
 * pulling the tasks from the head of the queue in a single thread.
 * <p/>
 * If the queue capacity is full, tasks are dropped and a warning is logged.
 * <p/>
 * Note that {@link #start()} must be called in order to start processing the queue and {@link #stop()} should be called
 * before the application is shut down.
 */
public abstract class SingleThreadedWriter extends AbstractScheduledService implements DatabaseWriter {

    private static final Logger LOG = LoggerFactory.getLogger(SingleThreadedWriter.class);
    private static final int LOGGING_INTERVAL_MS = 5000;
    public static final int DEFAULT_QUEUE_CAPACITY = 10000;

    private final int queueCapacity;
    protected final LinkedBlockingQueue<RunnableFuture<?>> queue;
    protected final GraphDatabaseService database;
    private final ScheduledExecutorService queueSizeLogger = Executors.newSingleThreadScheduledExecutor();

    /**
     * Construct a new writer with a default queue capacity of 10,000.
     *
     * @param database to write to.
     */
    protected SingleThreadedWriter(GraphDatabaseService database) {
        this(database, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Construct a new writer.
     *
     * @param database      to write to.
     * @param queueCapacity capacity of the queue.
     */
    protected SingleThreadedWriter(GraphDatabaseService database, int queueCapacity) {
        this.database = database;
        this.queueCapacity = queueCapacity;
        queue = new LinkedBlockingQueue<>(queueCapacity);
    }

    /**
     * Start the processing of tasks.
     */
    @PostConstruct
    public void start() {
        startAsync();
        awaitRunning();
        queueSizeLogger.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (queue.size() > 0 || logEmptyQueue()) {
                    LOG.info("Queue size: " + queue.size());
                }
            }
        }, 5, loggingFrequencyMs(), TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the processing of tasks.
     */
    @PreDestroy
    public void stop() {
        queueSizeLogger.shutdownNow();
        stopAsync();
        awaitTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutDown() throws Exception {
        runOneIteration();
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
        if (!state().equals(State.NEW) && !state().equals(State.STARTING) && !state().equals(State.RUNNING)) {
            throw new IllegalStateException("Database writer is not running!");
        }

        RunnableFuture<T> futureTask = createTask(task);

        if (!offer(futureTask)) {
            LOG.warn("Could not write task " + id + " to queue as it is too full. We're losing writes now.");
            return null;
        }

        if (waitMillis <= 0) {
            //no need to wait, caller not interested in result
            return null;
        }

        return block(futureTask, id, waitMillis);
    }

    /**
     * Offer a task to the queue. Intended to be overridden. By default, don't wait and return <code>false</code> in
     * case the queue is full, otherwise return <code>true</code>.
     *
     * @param futureTask to offer to the queue.
     * @return true iff the task was accepted.
     */
    protected boolean offer(RunnableFuture<?> futureTask) {
        return queue.offer(futureTask);
    }

    /**
     * Create a runnable future from the given task.
     *
     * @param task task.
     * @return future.
     */
    protected abstract <T> RunnableFuture<T> createTask(final Callable<T> task);

    /**
     * Block until the given task is executed, or until a timeout occurs.
     *
     * @param futureTask to wait for.
     * @param id         of the task for logging.
     * @param waitMillis how long to wait before giving up.
     * @param <T>        type of the task's result.
     * @return result of the task. <code>null</code> if timed out.
     */
    protected final <T> T block(RunnableFuture<T> futureTask, String id, int waitMillis) {
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

    /**
     * Return <code>true</code> iff empty queue should be logged. Defaults to <code>false</code>, intended to be overridden.
     *
     * @return true iff empty queue should be logged.
     */
    protected boolean logEmptyQueue() {
        return false;
    }

    /**
     * How often in ms should the queue size be reported to the log.
     *
     * @return logging interval in ms. The default is {@link #LOGGING_INTERVAL_MS}, intended to be overridden.
     */
    protected long loggingFrequencyMs() {
        return LOGGING_INTERVAL_MS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SingleThreadedWriter that = (SingleThreadedWriter) o;

        if (queueCapacity != that.queueCapacity) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return queueCapacity;
    }
}
