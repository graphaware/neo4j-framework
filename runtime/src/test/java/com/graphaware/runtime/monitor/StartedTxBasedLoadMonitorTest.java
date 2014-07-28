package com.graphaware.runtime.monitor;

import com.graphaware.runtime.schedule.TimingStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.*;

/**
 * Integration test for {@link StartedTxBasedLoadMonitor}.
 */
public class StartedTxBasedLoadMonitorTest {

    private GraphDatabaseService database;
    private DatabaseLoadMonitor loadMonitor;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        loadMonitor = new StartedTxBasedLoadMonitor(database, new RunningWindowAverage(200, 2000));
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void readTransactionsShouldBeMonitored() throws InterruptedException {
        assertEquals(TimingStrategy.UNKNOWN, loadMonitor.getLoad());

        for (int i = 0; i < 10; i++) {
            try (Transaction tx = database.beginTx()) {
                //do nothing
                tx.success();
            }

            Thread.sleep(1);
            assertTrue(loadMonitor.getLoad() > 0);
        }

        assertTrue(loadMonitor.getLoad() > 0);
    }
}
