package com.graphaware.tx.manager;

import java.util.Collection;
import java.util.UUID;

public interface TransactionManager {

    /**
     * Starts a new GraphAware Transaction.
     * @return a new GraphAware Transaction, encapsulating a Neo4J transaction
     * that can be monitored by the the TransactionManager.
     */
    Transaction beginTx();

    /**
     * Returns a list of GraphAware Transactions currently active on the server
     * @return
     */
    Collection<Transaction> list();

    /**
     * Given a thread executing a transaction, returns the associated
     * graphaware Transaction
     * @param t A Thread which may or may not be executing a transaction
     * @return  The Transaction running on that thread, if any.
     */
    Transaction find(Thread t);

    /**
     * Given a unique reference to a GraphAware Transaction, returns
     * the GraphAware Transaction encapsulating the actual Neo4J
     * Transaction
     * @param key UUID representing the Transaction
     * @return Transaction
     */
    Transaction get(UUID key);

    /**
     * Given a unique reference to a GraphAware Transaction, aborts
     * the underlying Neo4J transaction. Can be used to abort a transaction
     * that is taking too long, or is otherwise hung or spinning its
     * wheels.
     * @param key UUID representing the Transaction
     * @return Transaction
     */
    void abort(UUID key);

    /**
     * The number of GraphAware Transactions currently running
     * on this server.
     * @return
     */
    int size();
}
