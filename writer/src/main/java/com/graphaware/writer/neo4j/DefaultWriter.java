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

package com.graphaware.writer.neo4j;

import com.graphaware.writer.Writer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * A {@link Neo4jWriter} that writes to the database using the same thread that is submitting the task and blocks
 * until the write is finished. In other words, this is no different from writing directly to the database.
 */
public class DefaultWriter extends BaseNeo4jWriter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultWriter.class);

    /**
     * Create a new writer.
     *
     * @param database to write to.
     */
    public DefaultWriter(GraphDatabaseService database) {
        super(database);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Note that waitInMillis is ignored. The thread blocks until the write is complete.
     */
    @Override
    public <T> T write(Callable<T> task, String id, int waitMillis) {
        T result;
        try (Transaction tx = database.beginTx()) {
            result = task.call();
            tx.success();
        } catch (Exception e) {
            LOG.warn("Execution threw and exception.", e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

        return result;
    }
}
