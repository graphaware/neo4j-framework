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

import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * {@link SingleThreadedWriter} that writes tasks in batches. This is more performant but dangerous,
 * since a single task's failure can roll back the whole batch. This is here for experiments, not for production.
 */
public class BatchWriter extends SingleThreadedWriter implements Neo4jWriter {

    private static final Log LOG = LoggerFactory.getLogger(BatchWriter.class);
    public static final int DEFAULT_BATCH_SIZE = 1000;
    private final int batchSize;

    /**
     * Construct a new writer with a default queue capacity of 10,000 and a batch size of 1,000.
     *
     * @param database to write to.
     */
    public BatchWriter(GraphDatabaseService database) {
        super(database);
        this.batchSize = DEFAULT_BATCH_SIZE;
    }

    /**
     * Construct a new writer.
     *
     * @param database      to write to.
     * @param queueCapacity capacity of the queue.
     * @param batchSize     batch size.
     */
    public BatchWriter(GraphDatabaseService database, int queueCapacity, int batchSize) {
        super(database, queueCapacity);
        this.batchSize = batchSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <T> RunnableFuture<T> createTask(Callable<T> task) {
        return new FutureTask<>(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void runOneIteration() throws Exception {
        if (queue.isEmpty()) {
            return;
        }

        List<RunnableFuture<?>> tasks = new LinkedList<>();
        queue.drainTo(tasks);

        new IterableInputBatchTransactionExecutor<>(database, batchSize, tasks, new UnitOfWork<RunnableFuture<?>>() {
            @Override
            public void execute(GraphDatabaseService database, RunnableFuture<?> input, int batchNumber, int stepNumber) {
                processInput(input);
            }
        }).execute();
    }

    /**
     * Perform the processing of the given {@link RunnableFuture}.
     * Can be overridden to add extra logging, timing, etc.
     *
     * @param input to process.
     */
    protected void processInput(RunnableFuture<?> input) {
        try {
            input.run();
            input.get();
        } catch (Exception e) {
            LOG.warn("Execution threw an exception.", e);
        }
    }
}
