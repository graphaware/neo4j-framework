package com.graphaware.generator.config;

import com.graphaware.generator.node.NodeCreator;
import com.graphaware.generator.relationship.RelationshipCreator;
import com.graphaware.generator.relationship.RelationshipGenerator;

/**
 * Basic implementation of {@link GeneratorConfiguration} where everything can be configured by constructor instantiation,
 * except for batch size, which defaults to 1000.
 */
public class BasicGeneratorConfiguration<C extends RelationshipGeneratorConfig, G extends RelationshipGenerator<C>> implements GeneratorConfiguration<C, G> {

    private final int numberOfNodes;
    private final NodeCreator nodeCreator;
    private final RelationshipCreator relationshipCreator;
    private final G relationshipGenerator;
    private final C config;

    public BasicGeneratorConfiguration(int numberOfNodes, NodeCreator nodeCreator, RelationshipCreator relationshipCreator, G relationshipGenerator, C config) {
        this.numberOfNodes = numberOfNodes;
        this.nodeCreator = nodeCreator;
        this.relationshipCreator = relationshipCreator;
        this.relationshipGenerator = relationshipGenerator;
        this.config = config;
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
    public G getRelationshipGenerator() {
        return relationshipGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public C getConfig() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBatchSize() {
        return 1000;
    }
}
