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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.IterableUtils.countNodes;
import static com.graphaware.test.util.TestUtils.Timed;
import static com.graphaware.test.util.TestUtils.time;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link com.graphaware.tx.executor.batch.MultiThreadedBatchTransactionExecutor}.
 */
public class MultiThreadedBatchTransactionExecutorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void resultShouldBeCorrectWhenExecutedInMultipleThreads() {
        BatchTransactionExecutor batchExecutor = new MultiThreadedBatchTransactionExecutor(new NoInputBatchTransactionExecutor(database, 100, 40000, CreateNode.getInstance()), 4);

        batchExecutor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(40000, countNodes(database));
        }
    }

    @Test
    @Ignore("Multi-threaded not faster for some reason in CI") //todo investigate
    public void executionShouldBeFasterWhenExecutedInMultipleThreads() {
        final BatchTransactionExecutor singleThreadedBatchExecutor = new NoInputBatchTransactionExecutor(database, 100, 40000, CreateNode.getInstance());
        final BatchTransactionExecutor multiThreadedBatchExecutor = new MultiThreadedBatchTransactionExecutor(new NoInputBatchTransactionExecutor(database, 100, 40000, CreateNode.getInstance()), 4);

        long singleThreadedTime = time(new Timed() {
            @Override
            public void time() {
                singleThreadedBatchExecutor.execute();
            }
        });

        long multiThreadedTime = time(new Timed() {
            @Override
            public void time() {
                multiThreadedBatchExecutor.execute();
            }
        });

        System.out.println("Multi threaded execution faster by " + Math.round(100.00 * multiThreadedTime / singleThreadedTime) + "%");

        assertTrue(singleThreadedTime > multiThreadedTime);
    }
}
