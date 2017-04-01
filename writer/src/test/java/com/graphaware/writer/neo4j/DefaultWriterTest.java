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
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link TxPerTaskWriter}.
 */
public class DefaultWriterTest extends EmbeddedDatabaseIntegrationTest {

    private Neo4jWriter writer;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        writer = new DefaultWriter(getDatabase());
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

    @Test(expected = RuntimeException.class)
    public void runtimeExceptionFromTaskGetsPropagated() {
        writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                throw new RuntimeException("Deliberate Testing Exception");
            }
        }, "test", 50);
    }

    @Test(expected = RuntimeException.class)
    public void checkedExceptionFromTaskGetsTranslated() {
        writer.write(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                throw new IOException("Deliberate Testing Exception");
            }
        }, "test", 10);
    }
}
