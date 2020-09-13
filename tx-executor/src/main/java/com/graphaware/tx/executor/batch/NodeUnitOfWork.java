package com.graphaware.tx.executor.batch;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public abstract class NodeUnitOfWork implements UnitOfWork<Node> {

    @Override
    public void execute(Transaction tx, Node input, int batchNumber, int stepNumber) {
        execute(tx.getNodeById(input.getId()), batchNumber, stepNumber);
    }

    protected abstract void execute(Node input, int batchNumber, int stepNumber);
}
