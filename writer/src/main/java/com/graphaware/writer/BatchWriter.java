package com.graphaware.writer;

import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * {@link SingleThreadedWriter} that writes tasks in batches. This is more performant but dangerous,
 * since a single task's failure can roll back the whole batch. This is here for experiments, not for production.
 */
public class BatchWriter extends SingleThreadedWriter implements DatabaseWriter {

    private static final Logger LOG = LoggerFactory.getLogger(BatchWriter.class);
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
    protected void runOneIteration() throws Exception {
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
