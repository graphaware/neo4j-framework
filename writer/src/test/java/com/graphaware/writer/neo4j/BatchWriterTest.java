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

package com.graphaware.writer.neo4j;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test for {@link TxPerTaskWriter}.
 */
public class BatchWriterTest extends EmbeddedDatabaseIntegrationTest {

    private Neo4jWriter writer;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        writer = new BatchWriter(getDatabase());
        writer.start();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        writer.stop();
    }

    @Test
    public void shouldExecuteRunnable() {
        writer.write(() -> getDatabase().createNode());

        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(1, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void shouldNotWaitForResult() {
        Boolean result = writer.write(() -> {
            Thread.sleep(50);
            getDatabase().createNode();
            return true;
        }, "test", 0);

        assertNull(result);
        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(0, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }

        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(1, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void shouldWaitForResult() {
        Boolean result = writer.write(() -> {
            getDatabase().createNode();
            return true;
        }, "test", 200);

        assertTrue(result);

       waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(1, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void shouldNotWaitForLongTakingResult() {
        Boolean result = writer.write(() -> {
            Thread.sleep(20);
            getDatabase().createNode();
            return true;
        }, "test", 10);

        assertNull(result);
        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(0, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }

        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(1, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void whenQueueIsFullTasksGetDropped() {
        writer = new BatchWriter(getDatabase(), 2, 100);
        writer.start();

        Runnable task = () -> getDatabase().createNode();

        for (int i = 0; i < 10; i++) {
            writer.write(task);
        }

        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertTrue(10 > IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test(expected = RuntimeException.class)
    public void runtimeExceptionFromTaskGetsPropagatedIfWaiting() {
        writer.write(() -> {
            throw new RuntimeException("Deliberate Testing Exception");
        }, "test", 50);
    }

    @Test(expected = RuntimeException.class)
    public void checkedExceptionFromTaskGetsTranslatedIfWaiting() {
        writer.write(() -> {
            throw new IOException("Deliberate Testing Exception");
        }, "test", 20);
    }

    @Test
    public void runtimeExceptionFromTaskGetsIgnoredIfNotWaiting() {
        writer.write(() -> {
            throw new RuntimeException("Deliberate Testing Exception");
        }, "test", 0);

        writer.write(() -> {
            throw new RuntimeException("Deliberate Testing Exception");
        });
    }

    @Test
    public void oneFailingTransactionDoesntRollbackTheWholeBatch() {
        writer.write(() -> getDatabase().createNode());
        writer.write(() -> getDatabase().createNode());
        writer.write(() -> {
            throw new RuntimeException("Deliberate Testing Exception");
        });
        writer.write(() -> getDatabase().createNode());
        writer.write(() -> getDatabase().createNode());
        writer.write(() -> getDatabase().createNode());

        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(5, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void constraintViolationRollsBackTheWholeBatch() {
        try (Transaction tx = getDatabase().beginTx()) {
            getDatabase().createNode().createRelationshipTo(getDatabase().createNode(), RelationshipType.withName("test"));
            tx.success();
        }

        for (int i = 0; i < 5; i++) {
            writer.write(() -> getDatabase().createNode());
        }

        writer.write(() -> getDatabase().getNodeById(0).delete());

        for (int i = 0; i < 5; i++) {
            writer.write(() -> getDatabase().createNode());
        }

        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertTrue(10 > IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void multipleThreadsCanSubmitTasks() {
        writer = new BatchWriter(getDatabase());
        writer.start();

        final Runnable task = () -> getDatabase().createNode();

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> writer.write(task));

        }

        waitABit();
        waitABit();
        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(100, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void tasksAreFinishedBeforeShutdown() throws InterruptedException {
        writer = new BatchWriter(getDatabase());
        writer.start();

        final Runnable task = () -> getDatabase().createNode();

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> writer.write(task));
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        writer.stop();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(100, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    private void waitABit() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
