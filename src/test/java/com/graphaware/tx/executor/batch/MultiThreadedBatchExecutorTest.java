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

import com.graphaware.test.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.ImpermanentGraphDatabase;

import static com.graphaware.test.IterableUtils.countNodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link IterableInputBatchExecutor}.
 */
public class MultiThreadedBatchExecutorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new ImpermanentGraphDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void resultShouldBeCorrectWhenExecutedInMultipleThreads() {
        BatchExecutor batchExecutor = new MultiThreadedBatchExecutor(new NoInputBatchExecutor(database, 100, 40000, CreateNode.getInstance()), 4);

        batchExecutor.execute();

        assertEquals(40001, countNodes(database));
    }

    @Test
    @Ignore("Multi-threaded not faster for some reason in CI") //todo investigate
    public void executionShouldBeFasterWhenExecutedInMultipleThreads() {
        final BatchExecutor singleThreadedBatchExecutor = new NoInputBatchExecutor(database, 100, 40000, CreateNode.getInstance());
        final BatchExecutor multiThreadedBatchExecutor = new MultiThreadedBatchExecutor(new NoInputBatchExecutor(database, 100, 40000, CreateNode.getInstance()), 4);

        long singleThreadedTime = TestUtils.time(new TestUtils.Timed() {
            @Override
            public void time() {
                singleThreadedBatchExecutor.execute();
            }
        });

        long multiThreadedTime = TestUtils.time(new TestUtils.Timed() {
            @Override
            public void time() {
                multiThreadedBatchExecutor.execute();
            }
        });

        System.out.println("Multi threaded execution faster by " + Math.round(100.00 * multiThreadedTime / singleThreadedTime) + "%");
        assertTrue(singleThreadedTime > multiThreadedTime);
    }
}
