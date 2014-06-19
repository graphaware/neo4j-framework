package com.graphaware.generator.config;

import com.graphaware.generator.distribution.DegreeDistribution;
import com.graphaware.generator.node.NodeCreator;
import com.graphaware.generator.relationship.RelationshipCreator;
import com.graphaware.generator.relationship.RelationshipGenerator;

/**
 * A configuration of a {@link com.graphaware.generator.GraphGenerator}.
 */
public interface GeneratorConfiguration {

    NodeCreator getNodeCreator();

    RelationshipCreator getRelationshipCreator();

    RelationshipGenerator getRelationshipGenerator();

    DegreeDistribution getDegreeDistribution();

    int getBatchSize();
}
