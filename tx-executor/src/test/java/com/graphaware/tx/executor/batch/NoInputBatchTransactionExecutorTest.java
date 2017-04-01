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

import com.graphaware.tx.executor.NullItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.common.util.IterableUtils.countNodes;
import static org.junit.Assert.assertEquals;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Unit test for {@link com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor}.
 */
public class NoInputBatchTransactionExecutorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void whenBatchSizeDividesNumberOfStepsThenAllStepsShouldBeExecuted() {
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, 3, 6, CreateNode.getInstance());

        batchExecutor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(6, countNodes(database));
        }
    }

    @Test
    public void whenBatchSizeDoesNotNumberOfStepsThenAllStepsShouldBeExecuted() {
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, 5, 6, CreateNode.getInstance());

        batchExecutor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(6, countNodes(database));
        }
    }

    @Test
    public void whenBatchSizeGreaterThanNumberOfStepsThenAllStepsShouldBeExecuted() {
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, 7, 6, CreateNode.getInstance());

        batchExecutor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(6, countNodes(database));
        }
    }

    @Test
    public void whenStepSometimesThrowsAnExceptionThenOnlySomeBatchesShouldBeSuccessful() {
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, 3, 10, new ExceptionThrowingUnitOfWork(6));

        batchExecutor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(7, countNodes(database));  //1,2,3,7,8,9,10 (batch 4,5,6 is rolled back)
        }
    }

    @Test
    public void whenStepSometimesThrowsAnExceptionThenOnlySomeBatchesShouldBeSuccessful2() {
        BatchTransactionExecutor batchExecutor = new NoInputBatchTransactionExecutor(database, 3, 10, new ExceptionThrowingUnitOfWork(4));

        batchExecutor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(8, countNodes(database));  //1,2,3,5,6,7,9,10
        }
    }

    private static class ExceptionThrowingUnitOfWork implements UnitOfWork<NullItem> {
        private final int exceptionRate; //every exceptionRate(th) step will throw an exception
        private int steps = 0;

        private ExceptionThrowingUnitOfWork(int exceptionRate) {
            this.exceptionRate = exceptionRate;
        }

        @Override
        public void execute(GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
            steps++;
            if (steps % exceptionRate == 0) {
                throw new RuntimeException("Testing exception");
            } else {
                database.createNode();
            }
        }
    }

}
