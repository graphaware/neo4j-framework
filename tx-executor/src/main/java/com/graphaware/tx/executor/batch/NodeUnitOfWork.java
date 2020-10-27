package com.graphaware.tx.executor.batch;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * {@link UnitOfWork} which takes a Neo4j {@link Node} as input.
 */
public abstract class NodeUnitOfWork implements UnitOfWork<Node> {

    @Override
    public void execute(Transaction tx, Node input, int batchNumber, int stepNumber) {
        execute(tx.getNodeById(input.getId()), batchNumber, stepNumber);
    }

    /**
     * Execute the unit of work.
     *
     * @param input       to the unit of work.
     * @param batchNumber current batch number.
     * @param stepNumber  current step number.
     */
    protected abstract void execute(Node input, int batchNumber, int stepNumber);
}
