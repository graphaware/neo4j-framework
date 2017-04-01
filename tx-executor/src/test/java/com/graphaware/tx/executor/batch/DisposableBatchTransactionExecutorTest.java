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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link com.graphaware.tx.executor.batch.DisposableBatchTransactionExecutor}.
 */
public class DisposableBatchTransactionExecutorTest {

    @Test
    public void shouldExecuteForTheFirstTime() {
        DummyDisposableBatchTransactionExecutor executor = new DummyDisposableBatchTransactionExecutor();

        executor.execute();

        assertEquals(1, executor.getNoTimesExecuted());
    }

    @Test
    public void shouldNotExecuteMoreThanOnce() {
        DummyDisposableBatchTransactionExecutor executor = new DummyDisposableBatchTransactionExecutor();

        executor.execute();
        try {
            executor.execute();
            fail();
        } catch (IllegalStateException e) {
            //expected
        }

        assertEquals(1, executor.getNoTimesExecuted());
    }

    private static class DummyDisposableBatchTransactionExecutor extends DisposableBatchTransactionExecutor {

        AtomicInteger noTimesExecuted = new AtomicInteger(0);

        @Override
        protected void doExecute() {
            noTimesExecuted.incrementAndGet();
        }

        private int getNoTimesExecuted() {
            return noTimesExecuted.get();
        }
    }
}
