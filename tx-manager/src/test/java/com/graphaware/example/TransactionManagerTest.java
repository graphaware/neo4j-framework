package com.graphaware.example;

import com.graphaware.tx.manager.Transaction;
import com.graphaware.tx.manager.TransactionManager;
import com.graphaware.tx.manager.TransactionManagerImpl;
import com.graphaware.tx.manager.exception.TransactionAbortedException;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

public class TransactionManagerTest {

    private static final GraphDatabaseService database = new TestGraphDatabaseFactory()
            .newImpermanentDatabaseBuilder()
            //.loadPropertiesFromFile(this.getClass().getClassLoader().getResource("neo4j-transaction-monitor.properties").getPath())
            .setConfig(GraphDatabaseSettings.execution_guard_enabled, Boolean.TRUE.toString())
            .newGraphDatabase();

    private static final TransactionManager transactionManager = new TransactionManagerImpl(database);

    @Test
    public void testNoTransactionsOnIdleDatabase() {
        assertEquals(0, transactionManager.size());
    }

    @Test
    public void testLongRunningTransactionCanBeDetected() {
        final Thread thread = startLongRunningTransaction(5000);
        waitOnTransactionOpen(thread);
        assertEquals(1, transactionManager.size());
        waitOnTransactionClose(thread);
    }

    @Test
    public void testRunawayTransactionCanBeAborted() {

        final Thread thread = startRunawayTransaction();
        long now = -System.currentTimeMillis();

        waitOnTransactionOpen(thread);

        Transaction tx = transactionManager.find(thread);
        tx.abort();

        waitOnTransactionClose(thread);

        now += System.currentTimeMillis();
        assertTrue(now < 1000);
    }

    @Test
    public void testTransactionIsRemovedOnCompletion() {

        final Thread thread = startLongRunningTransaction(5000);

        waitOnTransactionOpen(thread);

        assertNotNull(transactionManager.find(thread));

        waitOnTransactionClose(thread);

        assertNull(transactionManager.find(thread));
    }

    @Test
    public void testTransactionIsRemovedOnRollback() {

        final Thread thread = startRollbackTransaction(5000);

        waitOnTransactionOpen(thread);

        assertNotNull(transactionManager.find(thread));

        waitOnTransactionClose(thread);

        assertNull(transactionManager.find(thread));
    }

    @Test
    public void testTransactionCanBeAbortedViaItsUUID() {
        final Thread thread = startRunawayTransaction();

        waitOnTransactionOpen(thread);

        Transaction tx = transactionManager.find(thread);

        transactionManager.abort(tx.getUuid());

        waitOnTransactionClose(thread);

        assertNull(transactionManager.get(tx.getUuid()));

    }

    /**
     * The transaction running on this thread simulates a long-running transaction: it creates
     * a single node then just waits for the specified time to elapse before calling tx.success()
     *
     * Because of the required interaction between the the transaction thread and the test harness, we
     * require the thread to notify us after the transaction is started. In normal operation of
     * course this is not necessary.
     * @return a new Thread in which a transaction is running
     */
    private Thread startLongRunningTransaction(final int milliseconds) {

        Thread t = new Thread() {
            public void run() {
                try (Transaction tx = transactionManager.beginTx()) {
                    database.createNode();
                    synchronized (this) {
                        notify();
                    }
                    try {
                        Thread.sleep(milliseconds);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    tx.success();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        };
        t.start();
        return t;
    }

    /**
     * The transaction running on this thread simulates a long-running transaction that is eventually
     * rolled back: it creates a single node then just waits for the specified time to elapse before
     * calling tx.failure()
     *
     * Because of the required interaction between the the transaction thread and the test harness, we
     * require the thread to notify us after the transaction is started. In normal operation of
     * course this is not necessary.
     * @return a new Thread in which a transaction is running
     */
    private Thread startRollbackTransaction(final int milliseconds) {

        Thread t = new Thread() {
            public void run() {
                try (Transaction tx = transactionManager.beginTx()) {
                    database.createNode();
                    synchronized (this) {
                        notify();
                    }
                    try {
                        Thread.sleep(milliseconds);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    tx.failure();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        };
        t.start();
        return t;
    }
    /**
     * The transaction running on this thread represents a runaway transaction: it won't terminate
     * unless it is aborted, or until some fundamental resource such as memory becomes unavailable.
     * Because of the required interaction between the the transaction thread and the test harness, we
     * require the thread to notify us after the transaction is started. In normal operation of
     * course this is not necessary.
     * @return a new Thread in which a transaction is running.
     */
    private Thread startRunawayTransaction() {

        Thread t = new Thread() {
            public void run() {
                try (Transaction tx = transactionManager.beginTx()) {
                    synchronized (this) {
                        notifyAll();
                    }

                    for (;;database.createNode());

                } catch (TransactionAbortedException e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }
        };
        t.start();
        return t;
    }

    private void waitOnTransactionOpen(Thread t) {
        try {
            synchronized(t) { t.wait(); };
        } catch (InterruptedException e) {
            ;
        }
    }

    private void waitOnTransactionClose(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            ;
        }
    }

}
