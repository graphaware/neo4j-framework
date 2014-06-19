package com.graphaware.generator.config;

import com.graphaware.generator.distribution.DegreeDistribution;
import com.graphaware.generator.node.NodeCreator;
import com.graphaware.generator.relationship.RelationshipCreator;
import com.graphaware.generator.relationship.RelationshipGenerator;

/**
 * Basic implementation of {@link GeneratorConfiguration} where everything can be configured by constructor instantiation,
 * except for batch size, which defaults to 1000.
 */
public class BasicGeneratorConfiguration implements GeneratorConfiguration {

    private final NodeCreator nodeCreator;
    private final RelationshipCreator relationshipCreator;
    private final RelationshipGenerator relationshipGenerator;
    private final DegreeDistribution degreeDistribution;

    public BasicGeneratorConfiguration(NodeCreator nodeCreator, RelationshipCreator relationshipCreator, RelationshipGenerator relationshipGenerator, DegreeDistribution degreeDistribution) {
        this.nodeCreator = nodeCreator;
        this.relationshipCreator = relationshipCreator;
        this.relationshipGenerator = relationshipGenerator;
        this.degreeDistribution = degreeDistribution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeCreator getNodeCreator() {
        return nodeCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipCreator getRelationshipCreator() {
        return relationshipCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipGenerator getRelationshipGenerator() {
        return relationshipGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DegreeDistribution getDegreeDistribution() {
        return degreeDistribution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBatchSize() {
        return 1000;
    }
}
