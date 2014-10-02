package com.graphaware.tx.writer;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * {@link SingleThreadedWriter} that writes each task in a separate transaction.
 */
public class TxPerTaskWriter extends SingleThreadedWriter implements DatabaseWriter {

    private static final Logger LOG = LoggerFactory.getLogger(TxPerTaskWriter.class);

    /**
     * Construct a new writer with a default queue capacity of 10,000.
     *
     * @param database to write to.
     */
    public TxPerTaskWriter(GraphDatabaseService database) {
        super(database);
    }

    /**
     * Construct a new writer with.
     *
     * @param database      to write to.
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
        logQueueSizeIfNeeded();

        try {
            RunnableFuture<?> r = queue.poll();

            if (r == null) {
                return;
            }

            while (r != null) {
                r.run();

                logQueueSizeIfNeeded();

                r = queue.poll();
            }
        } catch (Exception e) {
            LOG.error("Error running from the queue", e);
        }
    }
}
