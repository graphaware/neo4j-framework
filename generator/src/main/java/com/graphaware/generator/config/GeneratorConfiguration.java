package com.graphaware.generator.config;

import com.graphaware.generator.node.NodeCreator;
import com.graphaware.generator.relationship.RelationshipCreator;
import com.graphaware.generator.relationship.RelationshipGenerator;

/**
 * A configuration of a {@link com.graphaware.generator.GraphGenerator}.
 */
public interface GeneratorConfiguration {

    /**
     * Get the total number of nodes that will be generated.
     *
     * @return number of nodes.
     */
    int getNumberOfNodes();

    /**
     * Get the component generating relationships.
     *
     * @return relationship generator.
     */
    RelationshipGenerator<?> getRelationshipGenerator();

    /**
     * Get the component creating (populating) nodes.
     *
     * @return node creator.
     */
    NodeCreator getNodeCreator();

    /**
     * Get the component creating (populating) relationships.
     *
     * @return relationship creator.
     */
    RelationshipCreator getRelationshipCreator();

    /**
     * Get the batch size for graph creation in the database.
     *
     * @return batch size.
     */
    int getBatchSize();
}
