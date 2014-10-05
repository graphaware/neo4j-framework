package com.graphaware.writer;

import org.neo4j.graphdb.GraphDatabaseService;

import java.util.concurrent.Callable;

/**
 * A database writer that writes to the database in a way that prevents deadlocks.
 * <p/>
 * Implementations can choose how they write to the database, but must make sure that tasks that are submitted to it
 * run within the context of a transaction.
 */
public interface DatabaseWriter {

    /**
     * Write to the database without waiting for the result of the write.
     *
     * @param database to write to.
     * @param task     that writes to the database.
     */
    void write(GraphDatabaseService database, Runnable task);

    /**
     * Write to the database without waiting for the result of the write.
     *
     * @param database to write to.
     * @param task     that writes to the database.
     * @param id       of the task for logging purposes.
     */
    void write(GraphDatabaseService database, Runnable task, String id);

    /**
     * Write to the database.
     *
     * @param database   to write to.
     * @param task       that writes to the database and returns a result.
     * @param id         of the task for logging purposes.
     * @param waitMillis maximum number of ms to wait for the task to be executed.
     * @param <T>        type of the tasks return value.
     * @return value returned by the task. <code>null</code> of the tasks didn't complete in the specified waiting time,
     *         or if it didn't execute successfully.
     */
    <T> T write(GraphDatabaseService database, Callable<T> task, String id, int waitMillis);
}
