package com.graphaware.module.relcount.count;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * A singleton implementation of {@link WeighingStrategy} that gives each relationship a weight of 1.
 */
public final class OneForEach implements WeighingStrategy {

    private static final OneForEach INSTANCE = new OneForEach();

    private OneForEach() {
    }

    public static OneForEach getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
        return 1;
    }
}
