package com.graphaware.generator.config;

/**
 * Erdos Renyi graph generator configuration
 */
public class ErdosRenyiConfig extends NumberOfNodes {

    private final int numberOfEdges;

    /**
     * Constructs a new config.
     *
     * @param numberOfNodes number of nodes present in the network
     * @param numberOfEdges number of edges present in the network
     */
    public ErdosRenyiConfig(int numberOfNodes, int numberOfEdges) {
        super(numberOfNodes);
        this.numberOfEdges = numberOfEdges;
    }

    public int getNumberOfEdges() { return numberOfEdges;}

    /**
     * Tests if the config is valid
     * @return true if the configuration is valid.
     */
    @Override
    public boolean isValid() {
        return super.isValid() && numberOfEdges <= 0.5 * getNumberOfNodes() * (getNumberOfNodes() -1);
    }
}
