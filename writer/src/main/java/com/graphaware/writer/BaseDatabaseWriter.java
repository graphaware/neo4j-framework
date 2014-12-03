package com.graphaware.writer;

import org.neo4j.graphdb.GraphDatabaseService;

import static java.util.concurrent.Executors.callable;

/**
 * Abstract base class for {@link DatabaseWriter} implementations.
 */
public abstract class BaseDatabaseWriter implements DatabaseWriter {

    protected final GraphDatabaseService database;

    /**
     * Create a new database writer.
     *
     * @param database to write to.
     */
    protected BaseDatabaseWriter(GraphDatabaseService database) {
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
