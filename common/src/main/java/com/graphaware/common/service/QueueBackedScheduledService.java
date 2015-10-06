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

package com.graphaware.common.service;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * A {@link AbstractScheduledService} that maintains a queue of tasks and executes them in a single thread by constantly
 * pulling the tasks from the head of the queue.
 * <p/>
 * By default, if the queue capacity is full, tasks are dropped and a warning is logged.
 * <p/>
 * Note that {@link #start()} must be called in order to start processing the queue and {@link #stop()} should be called
 * before the application is shut down.
 */
public abstract class QueueBackedScheduledService extends AbstractScheduledService {

    private static final Logger LOG = LoggerFactory.getLogger(QueueBackedScheduledService.class);
    private static final int LOGGING_INTERVAL_MS = 5000;
    public static final int DEFAULT_QUEUE_CAPACITY = 10000;

    private final int queueCapacity;
    protected final LinkedBlockingQueue<RunnableFuture<?>> queue;
    private final ScheduledExecutorService queueSizeLogger = Executors.newSingleThreadScheduledExecutor();

    /**
     * Construct a new service with a default queue capacity of {@link #DEFAULT_QUEUE_CAPACITY}.
     */
    protected QueueBackedScheduledService() {
        this(DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Construct a new service.
     *
     * @param queueCapacity capacity of the queue.
     */
    protected QueueBackedScheduledService(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        queue = new LinkedBlockingQueue<>(queueCapacity);
    }

    /**
     * Start the processing of tasks.
     */
    public void start() {
        startAsync();
        awaitRunning();
        queueSizeLogger.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (logEmptyQueue() || !queue.isEmpty()) {
                    LOG.info("Queue size: " + queue.size());
                }
            }
        }, 5, loggingFrequencyMs(), TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the processing of tasks.
     */
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
     * Offer a task to the queue for processing. Intended to be overridden. By default, don't wait and return <code>false</code> in
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueueBackedScheduledService that = (QueueBackedScheduledService) o;

        if (queueCapacity != that.queueCapacity) {
            return false;
        }

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
