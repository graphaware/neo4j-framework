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

package com.graphaware.writer.service;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link AbstractScheduledService} that maintains a queue of tasks and executes them in a single thread by constantly
 * pulling the tasks from the head of the queue.
 * <p/>
 * By default, if the queue capacity is full, tasks are dropped and a warning is logged.
 * <p/>
 * Note that {@link #start()} must be called in order to start processing the queue and {@link #stop()} should be called
 * before the application is shut down.
 */
public abstract class QueueBackedScheduledService<E> extends AbstractScheduledService {

    private static final Log LOG = LoggerFactory.getLogger(QueueBackedScheduledService.class);
    private static final int LOGGING_INTERVAL_MS = 5000;
    public static final int DEFAULT_QUEUE_CAPACITY = 10000;

    private final int queueCapacity;
    protected final LinkedBlockingDeque<E> queue;
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
        queue = new LinkedBlockingDeque<>(queueCapacity);
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
     * Offer a task to the queue for processing. Intended to be overridden. By default, don't wait and return <code>false</code> in
     * case the queue is full, otherwise return <code>true</code>.
     *
     * @param futureTask to offer to the queue.
     * @return true iff the task was accepted.
     */
    protected boolean offer(E futureTask) {
        return queue.offer(futureTask);
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
