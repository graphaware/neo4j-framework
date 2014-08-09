package com.graphaware.common.strategy;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link RelationshipInclusionStrategy} that also takes into account the {@link Node} looking at the relationship.
 */
public interface NodeCentricRelationshipInclusionStrategy extends RelationshipInclusionStrategy {

    /**
     * Include the given relationships from the given node's point of view?
     *
     * @param relationship to make a decision on.
     * @param pointOfView  node looking at the relationship. Must be one of the relationship's nodes.
     * @return true to include, false to exclude.
     */
    boolean include(Relationship relationship, Node pointOfView);
}
