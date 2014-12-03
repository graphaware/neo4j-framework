package com.graphaware.writer;

import com.graphaware.common.util.IterableUtils;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.graphaware.writer.TxPerTaskWriter}.
 */
public class DefaultWriterTest extends DatabaseIntegrationTest {

    private DatabaseWriter writer;

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
