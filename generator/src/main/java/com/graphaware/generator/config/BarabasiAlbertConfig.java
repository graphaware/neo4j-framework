package com.graphaware.generator.config;

/**
 * {@link RelationshipGeneratorConfig} for {@link com.graphaware.generator.relationship.BarabasiAlbertGraphRelationshipGenerator}.
 */
public class BarabasiAlbertConfig extends NumberOfNodes {

    private final int edgesPerNewNode;

    /**
     * Construct a new config.
     *
     * @param numberOfNodes   number of nodes in the network.
     * @param edgesPerNewNode number of edges per newly added node.
     */
    public BarabasiAlbertConfig(int numberOfNodes, int edgesPerNewNode) {
        super(numberOfNodes);
        this.edgesPerNewNode = edgesPerNewNode;
    }

    public int getEdgesPerNewNode() {
        return edgesPerNewNode;
    }

    /**
     * Returns true iff the config is valid. Checks if number of edges is integer and the beta control parameter is valid.
     *
     * @return true if the parameter set is valid.
     */
    public boolean isValid() {
        // Necessary conditions.
        // TODO: Check thoroughly if these are sufficient as well.
        return !(edgesPerNewNode < 2 || edgesPerNewNode + 1 > getNumberOfNodes());
    }
}
