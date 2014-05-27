/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.tx.executor.batch;

import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.single.KeepCalmAndCarryOn;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import com.graphaware.tx.executor.single.TransactionExecutor;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link BatchTransactionExecutor} which executes a {@link UnitOfWork} for each input item. Input items are provided
 * in the form of an {@link Iterable}.
 *
 * @param <T> type of the input item, on which steps are executed.
 */
public class IterableInputBatchTransactionExecutor<T> implements BatchTransactionExecutor {
    private static final Logger LOG = Logger.getLogger(IterableInputBatchTransactionExecutor.class);

    private final int batchSize;
    private final UnitOfWork<T> unitOfWork;

    protected final AtomicInteger totalSteps = new AtomicInteger(0);
    protected final AtomicInteger batches = new AtomicInteger(0);
    protected final AtomicInteger successfulSteps = new AtomicInteger(0);
    protected final Iterator<T> iterator;
    protected final TransactionExecutor executor;

    /**
     * Create an instance of IterableInputBatchExecutor.
     *
     * @param database   against which to execute batched queries.
     * @param batchSize  how many {@link UnitOfWork} are in a single batch.
     * @param input      to the execution. These are provided to each unit of work, one by one.
     * @param unitOfWork to be executed for each input item. Must be thread-safe.
     */
    public IterableInputBatchTransactionExecutor(GraphDatabaseService database, int batchSize, Iterable<T> input, UnitOfWork<T> unitOfWork) {
        this.batchSize = batchSize;
        this.unitOfWork = unitOfWork;
        this.iterator = input.iterator();
        this.executor = new SimpleTransactionExecutor(database);
    }

    /**
     * Create an instance of IterableInputBatchExecutor.
     *
     * @param database   against which to execute batched queries.
     * @param batchSize  how many {@link UnitOfWork} are in a single batch.
     * @param callback   that will produce the input to the execution but needs to run in a transaction. Items of the input are provided to each unit of work, one by one.
     * @param unitOfWork to be executed for each input item. Must be thread-safe.
     */
    public IterableInputBatchTransactionExecutor(GraphDatabaseService database, int batchSize, TransactionCallback<Iterable<T>> callback, UnitOfWork<T> unitOfWork) {
        this.batchSize = batchSize;
        this.unitOfWork = unitOfWork;
        this.executor = new SimpleTransactionExecutor(database);
        this.iterator = executor.executeInTransaction(callback).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        while (hasNext()) {
            batches.incrementAndGet();

            if (LOG.isTraceEnabled()) {
                LOG.trace("Starting a transaction for batch number " + batches);
            }

            final AtomicInteger currentBatchSteps = new AtomicInteger(0);
            NullItem result = executor.executeInTransaction(new TransactionCallback<NullItem>() {
                @Override
                public NullItem doInTransaction(GraphDatabaseService database) {

                    while (iterator.hasNext() && currentBatchSteps.get() < batchSize) {
                        try {
                            currentBatchSteps.incrementAndGet();
                            totalSteps.incrementAndGet();
                            unitOfWork.execute(database, iterator.next(), batches.get(), currentBatchSteps.get());
                        } catch (NoSuchElementException e) {
                            //this is OK, another thread could have gotten the item after this one called hasNext().
                            //Simply means there's no more items to process.
                            break;
                        }
                    }
                    return NullItem.getInstance();

                }
            }, KeepCalmAndCarryOn.getInstance());


            if (result != null) {
                successfulSteps.addAndGet(currentBatchSteps.get());
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Committed transaction for batch number " + batches);
                }
            } else {
                LOG.warn("Rolled back transaction for batch number " + batches);
            }
        }

        LOG.info("Successfully executed " + successfulSteps + " (out of " + totalSteps.get() + " ) steps in " + batches + " batches");
        if (successfulSteps.get() != totalSteps.get()) {
            LOG.warn("Failed to execute " + (totalSteps.get() - successfulSteps.get()) + " steps!");
        }
    }

    private Boolean hasNext() {
        return executor.executeInTransaction(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(GraphDatabaseService database) {
                return iterator.hasNext();
            }
        });
    }
}
