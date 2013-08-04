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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Unit tests for {@link DisposableBatchExecutor}.
 */
public class DisposableBatchExecutorTest {

    @Test
    public void shouldExecuteForTheFirstTime() {
        DummyDisposableBatchExecutor executor = new DummyDisposableBatchExecutor();

        executor.execute();

        assertEquals(1, executor.getNoTimesExecuted());
    }

    @Test
    public void shouldNotExecuteMoreThanOnce() {
        DummyDisposableBatchExecutor executor = new DummyDisposableBatchExecutor();

        executor.execute();
        try {
            executor.execute();
            fail();
        } catch (IllegalStateException e) {
            //expected
        }

        assertEquals(1, executor.getNoTimesExecuted());
    }

    private static class DummyDisposableBatchExecutor extends DisposableBatchExecutor {

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
