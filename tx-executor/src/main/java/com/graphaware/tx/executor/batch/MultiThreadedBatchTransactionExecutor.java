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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Decorator for a {@link BatchTransactionExecutor}, which allows it to be executed using multiple threads.
 */
public class MultiThreadedBatchTransactionExecutor extends DisposableBatchTransactionExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadedBatchTransactionExecutor.class);

    private final BatchTransactionExecutor wrappedExecutor;
    private final int numberOfThreads;

    /**
     * Create a new instance of this executor.
     *
     * @param wrappedExecutor the executor to which each thread will delegate work.
     * @param numberOfThreads the total number of threads used for the execution.
     */
    public MultiThreadedBatchTransactionExecutor(BatchTransactionExecutor wrappedExecutor, int numberOfThreads) {
        this.wrappedExecutor = wrappedExecutor;
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute() {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    wrappedExecutor.execute();
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
            LOG.info("Successfully executed batches using " + numberOfThreads + " threads.");
        } catch (InterruptedException e) {
            LOG.warn("Did not manage to complete batch execution within 24 hours.");
        }
    }
}
