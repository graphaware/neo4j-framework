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

import com.graphaware.common.service.QueueBackedScheduledService;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Callable;

import static java.util.concurrent.Executors.callable;

/**
 * A {@link DatabaseWriter} that maintains a queue of tasks and writes to the database in a single thread by constantly
 * pulling the tasks from the head of the queue.
 * <p/>
 * If the queue capacity is full, tasks are dropped and a warning is logged.
 * <p/>
 * Note that {@link #start()} must be called in order to start processing the queue and {@link #stop()} should be called
 * before the application is shut down.
 */
public abstract class SingleThreadedWriter extends QueueBackedScheduledService implements DatabaseWriter {

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
}
