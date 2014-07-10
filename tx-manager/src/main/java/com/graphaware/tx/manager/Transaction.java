package com.graphaware.tx.manager;

import com.graphaware.tx.manager.exception.TransactionAbortedException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.kernel.guard.Guard;

import java.util.UUID;

/**
 * A GraphAware Transaction wraps a standard Neo4J Transaction, allowing it to be
 * monitored and/or aborted by the TransactionManager.
 *
 * All the normal transaction methods are available, so client code does not need to
 * change, apart from the way a transaction is obtained, e.g:
 *
 *    try (Transaction tx = database.beginTx()) { .... }
 *
 * should be changed to:
 *
 *    try (Transaction tx = TransactionManager.beginTx()) { ... }
 *
 * For Cypher (HTTP) based queries the transaction is implied, so in order to monitor
 * these, the execution engine must be intercepted or wrapped appropriately to provide the
 * necessary capability.
 */
public class Transaction implements Guard.GuardInternal, org.neo4j.graphdb.Transaction {

    private final org.neo4j.graphdb.Transaction transaction;
    private final UUID uuid;
    private final long started;
    private final Thread thread;
    private final TransactionManagerImpl manager;

    private boolean running = true;

    public Transaction(TransactionManagerImpl manager, GraphDatabaseService database) {
        this.uuid = UUID.randomUUID();
        this.started = System.currentTimeMillis();
        this.transaction = database.beginTx();
        this.manager = manager;
        this.thread = Thread.currentThread();
    }

    @Override
    public void failure() {
        transaction.failure();
        manager.stop(uuid);
    }

    public void success() {
        transaction.success();
        manager.stop(uuid);
    }

    @Override
    @Deprecated
    public void finish() {
        transaction.finish();
        manager.stop(uuid);
    }

    @Override
    public void close() {
        transaction.close();
        manager.stop(uuid);
    }

    @Override
    public Lock acquireWriteLock(PropertyContainer propertyContainer) {
        return transaction.acquireWriteLock(propertyContainer);
    }

    @Override
    public Lock acquireReadLock(PropertyContainer propertyContainer) {
        return transaction.acquireReadLock(propertyContainer);
    }

    /**
     * Allows another thread to wait for this transaction to terminate.
     * @throws InterruptedException
     */
    public void join() throws InterruptedException {
        thread.join();
    }

    /**
     * Returns the thread the transaction is running on. Required for
     * testing, but maybe nothing else.
     * @return
     */
    public Thread thread() {
        return thread;
    }

    /**
     * Allows a transaction to be aborted and rolled back. Note that this call is asynchronous
     * so this is actually a request to abort which we signal by changing the running status.
     * The guard will check the running status frequently, so rollback will normally happen soon
     * after the call to abort is made.
     */
    public void abort() {
        running = false;
        manager.abort(uuid);
    }

    /**
     * Neo4J's kernel guard calls this method frequently (if it is turned on).
     * By throwing an exception here we signal that the transaction should die and
     * the kernel rolls the transaction back.
     */
    @Override
    public void check() {
        if (!running) {
            throw new TransactionAbortedException("Transaction aborted:" + uuid);
        }
    }

    public org.neo4j.graphdb.Transaction getTransaction() {
        return transaction;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getStarted() {
        return started;
    }

    public boolean isRunning() {
        return running;
    }

    public long getElapsed() {
        return System.currentTimeMillis() - started;
    }
}
