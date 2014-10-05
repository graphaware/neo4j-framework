package com.graphaware.writer;

import org.neo4j.graphdb.GraphDatabaseService;

import static java.util.concurrent.Executors.callable;

/**
 * Abstract base class for {@link DatabaseWriter} implementations.
 */
public abstract class BaseDatabaseWriter implements DatabaseWriter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(GraphDatabaseService database, Runnable task) {
        write(database, task, "UNKNOWN");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(GraphDatabaseService database, Runnable task, String id) {
        write(database, callable(task), id, 0);
    }
}
