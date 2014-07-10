package com.graphaware.generator.config;

/**
 *
 */
public class NumberOfNodes implements RelationshipGeneratorConfig {

    private final int numberOfNodes;

    /**
     * Construct a new config.
     *
     * @param numberOfNodes number of nodes present in the network.
     */
    public NumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    /**
     * Tests if the config is valid.
     * TODO: Check if sufficient
     *
     * @return true iff the config is valid.
     */
    public boolean isValid() {
        return numberOfNodes >= 2;
    }
}
