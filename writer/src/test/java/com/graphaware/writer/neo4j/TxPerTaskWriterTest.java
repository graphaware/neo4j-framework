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
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test for {@link TxPerTaskWriter}.
 */
public class TxPerTaskWriterTest extends EmbeddedDatabaseIntegrationTest {

    private Neo4jWriter writer;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        writer = new TxPerTaskWriter(getDatabase());
        writer.start();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        writer.stop();
    }

    @Test
    public void shouldExecuteRunnable() {
        writer.write(new Runnable() {
            @Override
            public void run() {
                getDatabase().createNode();
            }
        });

        waitABit();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(1, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void shouldNotWaitForResult() {
        Boolean result = writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(50);
                getDatabase().createNode();
                return true;
            }
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
        Boolean result = writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                getDatabase().createNode();
                return true;
            }
        }, "test", 50);

        assertTrue(result);
        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(1, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void shouldNotWaitForLongTakingResult() {
        Boolean result = writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.sleep(20);
                getDatabase().createNode();
                return true;
            }
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
        writer = new TxPerTaskWriter(getDatabase(), 2);
        writer.start();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                getDatabase().createNode();
            }
        };

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
        writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                throw new RuntimeException("Deliberate Testing Exception");
            }
        }, "test", 50);
    }

    @Test(expected = RuntimeException.class)
    public void checkedExceptionFromTaskGetsTranslatedIfWaiting() {
        writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                throw new IOException("Deliberate Testing Exception");
            }
        }, "test", 50);
    }

    @Test
    public void runtimeExceptionFromTaskGetsIgnoredIfNotWaiting() {
        writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                throw new RuntimeException("Deliberate Testing Exception");
            }
        }, "test", 0);

        writer.write(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("Deliberate Testing Exception");
            }
        });
    }

    @Test
    public void multipleThreadsCanSubmitTasks() {
        writer = new TxPerTaskWriter(getDatabase());
        writer.start();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                getDatabase().createNode();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    writer.write(task);
                }
            });

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
        writer = new TxPerTaskWriter(getDatabase());
        writer.start();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                getDatabase().createNode();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    writer.write(task);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);

        writer.stop();

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(100, IterableUtils.countNodes(getDatabase()));
            tx.success();
        }
    }

    @Test
    public void callablesWork() throws InterruptedException {
        writer = new TxPerTaskWriter(getDatabase());
        writer.start();

        final Callable<Long> task = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Node test = getDatabase().createNode(Label.label("test"));
                test.setProperty("test", "test");
                return test.getId();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    writer.write(task, "test", 0);
                }
            });
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
