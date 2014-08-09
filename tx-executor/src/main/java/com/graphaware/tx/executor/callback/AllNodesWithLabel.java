package com.graphaware.tx.executor.callback;

import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * {@link TransactionCallback} returning all nodes with a specific label.
 */
public final class AllNodesWithLabel implements TransactionCallback<Iterable<Node>> {

    private final Label label;

    /**
     * Construct the callback.
     *
     * @param label which all returned nodes have.
     */
    public AllNodesWithLabel(Label label) {
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
        return GlobalGraphOperations.at(database).getAllNodesWithLabel(label);
    }
}
