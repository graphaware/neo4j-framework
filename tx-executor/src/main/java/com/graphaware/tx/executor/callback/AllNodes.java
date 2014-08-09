package com.graphaware.tx.executor.callback;

import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * {@link TransactionCallback} returning all nodes in the database. Singleton.
 */
public final class AllNodes implements TransactionCallback<Iterable<Node>> {

    private static final AllNodes INSTANCE = new AllNodes();

    /**
     * Get an instance of this callback.
     *
     * @return instance.
     */
    public static AllNodes getInstance() {
        return INSTANCE;
    }

    private AllNodes() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
        return GlobalGraphOperations.at(database).getAllNodes();
    }
}
