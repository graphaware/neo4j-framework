package com.graphaware.generator.config;

import com.graphaware.generator.node.NodeCreator;
import com.graphaware.generator.relationship.RelationshipCreator;
import com.graphaware.generator.relationship.RelationshipGenerator;

/**
 * Basic implementation of {@link GeneratorConfiguration} where everything can be configured by constructor instantiation,
 * except for batch size, which defaults to 1000.
 */
public class BasicGeneratorConfiguration implements GeneratorConfiguration {

    private final int numberOfNodes;
    private final RelationshipGenerator<?> relationshipGenerator;
    private final NodeCreator nodeCreator;
    private final RelationshipCreator relationshipCreator;

    public BasicGeneratorConfiguration(int numberOfNodes, RelationshipGenerator<?> relationshipGenerator, NodeCreator nodeCreator, RelationshipCreator relationshipCreator) {
        this.numberOfNodes = numberOfNodes;
        this.relationshipGenerator = relationshipGenerator;
        this.nodeCreator = nodeCreator;
        this.relationshipCreator = relationshipCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfNodes() {
        return numberOfNodes;
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
    public RelationshipGenerator<?> getRelationshipGenerator() {
        return relationshipGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBatchSize() {
        return 1000;
    }
}
