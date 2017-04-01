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

import com.graphaware.writer.Writer;
import org.neo4j.graphdb.GraphDatabaseService;

import static java.util.concurrent.Executors.callable;

/**
 * A Neo4j database {@link Writer}.
 * <p/>
 * Extending classes can choose how they write to the database, but must make sure that tasks that are submitted to it
 * run within the context of a transaction.
 */
public abstract class BaseNeo4jWriter implements Neo4jWriter {

    protected final GraphDatabaseService database;

    /**
     * Create a new database writer.
     *
     * @param database to write to.
     */
    protected BaseNeo4jWriter(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        //no-op by default
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        //no-op by default
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
}
