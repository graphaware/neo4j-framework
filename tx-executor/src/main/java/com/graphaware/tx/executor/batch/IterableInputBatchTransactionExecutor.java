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

package com.graphaware.tx.executor.batch;

import com.graphaware.common.util.BlockingArrayBlockingQueue;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.input.TransactionalInput;
import com.graphaware.tx.executor.single.KeepCalmAndCarryOn;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import com.graphaware.tx.executor.single.TransactionExecutor;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link BatchTransactionExecutor} which executes a {@link UnitOfWork} for each input item. Input items are provided
 * in the form of an {@link Iterable}.
 *
 * @param <T> type of the input item, on which steps are executed.
 */
public class IterableInputBatchTransactionExecutor<T> extends DisposableBatchTransactionExecutor {
    private static final Log LOG = LoggerFactory.getLogger(IterableInputBatchTransactionExecutor.class);

    private final int batchSize;
    private final UnitOfWork<T> unitOfWork;

    protected final AtomicInteger totalSteps = new AtomicInteger(0);
    protected final AtomicInteger batches = new AtomicInteger(0);
    protected final AtomicInteger successfulSteps = new AtomicInteger(0);
    protected final Iterable<T> input;
    protected final TransactionExecutor executor;

    protected final AtomicBoolean finished = new AtomicBoolean(false);
    protected final BlockingArrayBlockingQueue<T> queue = new BlockingArrayBlockingQueue<>(10_000);

    /**
     * Create an instance of IterableInputBatchExecutor.
     *
     * @param database   against which to execute batched queries.
     * @param batchSize  how many {@link UnitOfWork} are in a single batch.
     * @param input      to the execution. These items are provided to each unit of work, one by one.
     *                   Please use {@link TransactionalInput} if the input is fetched from the database.
     * @param unitOfWork to be executed for each input item. Must be thread-safe.
     */
    public IterableInputBatchTransactionExecutor(GraphDatabaseService database, int batchSize, Iterable<T> input, UnitOfWork<T> unitOfWork) {
        this.batchSize = batchSize;
        this.unitOfWork = unitOfWork;
        this.input = input;
        this.executor = new SimpleTransactionExecutor(database);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doExecute() {
        populateQueue();
        processQueue();
    }

    protected final void populateQueue() {
        new Thread(() -> {
            try {
                for (T input : IterableInputBatchTransactionExecutor.this.input) {
                    queue.offer(input);
                }
            } catch (Exception e) {
                LOG.warn("Exception while producing input!", e);
            } finally {
                finished.set(true);
            }
        }).start();
    }

    protected final void processQueue() {
        while (notFinished()) {
            final int batchNo = batches.incrementAndGet();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting a transaction for batch number " + batchNo);
            }

            final AtomicInteger currentBatchSteps = new AtomicInteger(0);
            final AtomicBoolean polled = new AtomicBoolean(false);
            NullItem result = executor.executeInTransaction(database -> {
                while ((notFinished()) && currentBatchSteps.get() < batchSize) {
                    T next;
                    try {
                        next = queue.poll(100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        continue;
                    }

                    if (next != null) {
                        polled.set(true);
                        totalSteps.incrementAndGet();
                        unitOfWork.execute(database, next, batchNo, currentBatchSteps.incrementAndGet());
                    } else {
                        if (!finished.get()) {
                            LOG.warn("Waited for over 100ms but no input arrived. Still expecting more input. ");
                        } else {
                            break;
                        }
                    }
                }
                return NullItem.getInstance();

            }, KeepCalmAndCarryOn.getInstance());

            if (result != null) {
                successfulSteps.addAndGet(currentBatchSteps.get());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Committed transaction for batch number " + batchNo);
                }
            } else {
                LOG.warn("Rolled back transaction for batch number " + batchNo);

                if (!polled.get()) {
                    LOG.warn("Throwing away the head of the queue as the transaction seems to have failed before polling...");
                    queue.poll();
                }
            }
        }

        LOG.debug("Successfully executed " + successfulSteps + " (out of " + totalSteps.get() + " ) steps in " + batches + " batches");
        if (successfulSteps.get() != totalSteps.get()) {
            LOG.warn("Failed to execute " + (totalSteps.get() - successfulSteps.get()) + " steps!");
        }
    }

    private boolean notFinished() {
        return !finished.get() || !queue.isEmpty();
    }
}
