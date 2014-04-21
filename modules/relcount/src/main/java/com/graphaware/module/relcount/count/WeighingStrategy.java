package com.graphaware.module.relcount.count;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * A strategy for determining the weight of a {@link Relationship} when counting relationships. This could, for
 * example, be a property on the {@link Relationship}, or it could be computed based on the {@link Node}s the
 * relationship connects.
 */
public interface WeighingStrategy {

    /**
     * Get a relationship's weight.
     *
     * @param relationship to find weight for.
     * @param pointOfView  node whose point of view we are currently looking at the relationship. This gives the opportunity
     *                     to determine relationship weight based on the other node's characteristics, for instance.
     * @return the relationship weight. Should be positive.
     */
    int getRelationshipWeight(Relationship relationship, Node pointOfView);
}
