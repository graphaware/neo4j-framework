/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.writer.neo4j;

import com.graphaware.writer.service.QueueBackedScheduledService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.callable;

/**
 * A {@link Neo4jWriter} that maintains a queue of tasks and writes to the database in a single thread by constantly
 * pulling the tasks from the head of the queue.
 * <p/>
 * If the queue capacity is full, tasks are dropped and a warning is logged.
 * <p/>
 * Note that {@link #start()} must be called in order to start processing the queue and {@link #stop()} should be called
 * before the application is shut down.
 */
public abstract class SingleThreadedWriter extends QueueBackedScheduledService<RunnableFuture<?>> implements Neo4jWriter {

    private static final Log LOG = LoggerFactory.getLogger(SingleThreadedWriter.class);
    protected final GraphDatabaseService database;

    /**
     * Construct a new writer with a default queue capacity of {@link #DEFAULT_QUEUE_CAPACITY}.
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
        super(queueCapacity);
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @PostConstruct
    public void start() {
        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void stop() {
        super.stop();
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
        return offer(task, id, waitMillis);
    }

    /**
     * Offer a task to the queue for processing.
     *
     * @param task       to process.
     * @param id         of the task for logging purposes.
     * @param waitMillis how many milliseconds to block and wait for the task to be executed. Use 0 (or less) for no blocking,
     *                   i.e. for situations where the caller isn't interested in the result.
     * @param <T>        type of the processing result.
     * @return result of the processing or <code>null</code> if the caller decided not to wait, or if the processing took
     * longer than the <code>waitMillis</code> argument, or if the queue is too full and the {@link #offer(RunnableFuture)} has
     * not been overridden to block in such situations.
     */
    protected final <T> T offer(final Callable<T> task, String id, int waitMillis) {
        if (!state().equals(State.NEW) && !state().equals(State.STARTING) && !state().equals(State.RUNNING)) {
            throw new IllegalStateException("Service is not running!");
        }

        RunnableFuture<T> futureTask = createTask(task);

        if (!offer(futureTask)) {
            LOG.warn("Could not write task " + id + " to queue as it is too full. We're losing taks now.");
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
}
