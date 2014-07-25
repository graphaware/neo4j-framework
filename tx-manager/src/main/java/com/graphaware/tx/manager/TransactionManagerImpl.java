package com.graphaware.tx.manager;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.guard.Guard;

import java.util.Collection;
import java.util.UUID;

/*
 * The Transaction Manager for a single Neo4J instance.
 *
 * In a clustered configuration we will need some way of querying all the servers in order to
 * enumerate their running transactions and then identify, for each one, which server it is running
 * on. This capability must be available from any arbitrary server in the cluster.
 *
 * Given the above, we'll then need some mechanism allowing us to hook back into the server that is running
 * a specific transaction, in order to ask its transaction manager to abort that transaction.
 *
 * This implies some sort of socket/messaging infrastructure allowing the TransactionManager on each node to
 * register with its peers running on other servers:  allowing them to request transaction info
 * from each other, as well as broadcast abort requests for transactions they don't own.
*/

public class TransactionManagerImpl implements TransactionManager {

    private final TransactionRegister transactionRegister;
    private final GraphDatabaseService database;
    private final Guard guard;

    public TransactionManagerImpl(GraphDatabaseService graphDatabaseService) {
        this.transactionRegister = new TransactionRegister();
        this.database = graphDatabaseService;
        // todo: there must be a better way to get this than using a deprecated class cast!
        this.guard = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(Guard.class);
    }

    public Transaction beginTx() {
        Transaction tx = new Transaction(this, database);
        guard.start(tx);
        transactionRegister.put(tx);
        return tx;
    }

    public Collection<Transaction> list() {
        return transactionRegister.transactions();
    }

    @Override
    public Transaction find(Thread t) {
        for (Transaction tx : transactionRegister.transactions()) {
            if (tx.thread() == t) {
                return tx;
            }
        }
        return null;
    }

    @Override
    public Transaction get(UUID key) {
        return transactionRegister.get(key);
    }

    /**
     * clients are permitted to abort a transaction either by calling TransactionMonitor.abort(...)
     * or by calling abort() directly on the Transaction. The latter option is possible if the database
     * is running in embedded mode.
     *
     * This code caters for properly aborting a transaction using either mechanism.
     * @param key
     */
    public void abort(UUID key) {
        try {
            Transaction transactionGuard = transactionRegister.get(key);
            if (transactionGuard != null) {
                transactionRegister.remove(key);
                if (transactionGuard.isRunning()) {
                    transactionGuard.abort();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Internal Error: could not abort transaction: " + key + ". Reason: " + e.getLocalizedMessage());
        }
    }

    @Override
    public int size() {
        return transactionRegister.size();
    }

    /**
     * note: package private. Don't change it.
     * @param key
     */
    void stop(UUID key) {
        transactionRegister.remove(key);
    }


}
