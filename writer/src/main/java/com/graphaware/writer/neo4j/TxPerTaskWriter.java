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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * {@link SingleThreadedWriter} that writes each task in a separate transaction.
 */
public class TxPerTaskWriter extends SingleThreadedWriter implements Neo4jWriter {

    private static final Log LOG = LoggerFactory.getLogger(TxPerTaskWriter.class);

    /**
     * Construct a new writer with a default queue capacity of 10,000.
     *
     * @param database to write to.
     */
    public TxPerTaskWriter(GraphDatabaseService database) {
        super(database);
    }

    /**
     * Construct a new writer.
     *
     * @param database to write to.
     * @param queueCapacity capacity of the queue.
     */
    public TxPerTaskWriter(GraphDatabaseService database, int queueCapacity) {
        super(database, queueCapacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <T> RunnableFuture<T> createTask(final Callable<T> task) {
        return new FutureTask<>(new Callable<T>() {
            @Override
            public T call() {
                try (Transaction tx = database.beginTx()) {
                    T result = task.call();
                    tx.success();
                    return result;
                } catch (Exception e) {
                    LOG.warn("Execution threw and exception.", e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runOneIteration() throws Exception {
        try {
            RunnableFuture<?> r = queue.poll();

            if (r == null) {
                return;
            }

            while (r != null) {
                r.run();
                r = queue.poll();
            }
        } catch (Exception e) {
            LOG.error("Error running from the queue", e);
        }
    }
}
