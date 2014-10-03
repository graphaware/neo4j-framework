package com.graphaware.tx.writer;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.concurrent.Callable;

import static java.util.concurrent.Executors.callable;

/**
 * A {@link DatabaseWriter} that writes to the database using the same thread that is submitting the task and blocks
 * until the write is finished. In other words, this is no different from writing directly to the database.
 */
public class DefaultWriter implements DatabaseWriter {

    private final GraphDatabaseService database;

    public DefaultWriter(GraphDatabaseService database) {
        this.database = database;
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
        write(callable(task), "UNKNOWN", Integer.MAX_VALUE);
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
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }

        return result;
    }
}
