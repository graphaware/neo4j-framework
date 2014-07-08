package com.graphaware.generator.config;

/**
 * {@link RelationshipGeneratorConfig} for {@link com.graphaware.generator.relationship.WattsStrogatzRelationshipGenerator}.
 */
public class WattsStrogatzConfig extends NumberOfNodes {

    private final int meanDegree;
    private final double beta;

    /**
     * Construct a new config.
     *
     * @param numberOfNodes number of nodes in the network.
     * @param meanDegree    ean degree of the regular ring network constructed as an initial step for Watts-Strogatz.
     * @param beta          probability of edge rewiring at a given step.
     */
    public WattsStrogatzConfig(int numberOfNodes, int meanDegree, double beta) {
        super(numberOfNodes);
        this.meanDegree = meanDegree;
        this.beta = beta;
    }

    public int getMeanDegree() {
        return meanDegree;
    }

    public double getBeta() {
        return beta;
    }

    /**
     * Returns true iff the config is valid. Checks if number of edges is integer and the beta control parameter is valid.
     *
     * @return true if the parameter set is valid
     */
    public boolean isValid() {
        return !(meanDegree % 2 != 0 ||
                meanDegree < 3 ||
                meanDegree > getNumberOfNodes() - 1) &&
                (0 <= beta && beta <= 1);
    }
}
