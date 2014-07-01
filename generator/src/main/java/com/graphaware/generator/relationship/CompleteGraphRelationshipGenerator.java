package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.distribution.DegreeDistribution;
import com.graphaware.generator.distribution.InvalidDistributionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a complete graph. Used for the core graph in
 * Barabasi-Albert network generator
 *
 * TODO: change the base-class such that it reflects the different parameter sets needed for the network
 */
public class CompleteGraphRelationshipGenerator extends BaseRelationshipGenerator{

    /**
     * Generates a completely connected (undirected) graph
     * @param numberOfNodes number of nodes present in the completely connected network
     * @return graph
     */
    public List<UnorderedPair<Integer>> doGenerateEdges(int numberOfNodes) {
        if (!isValidParameterSet(numberOfNodes))
            throw new InvalidDistributionException("The supplied parameter set is invalid");

        ArrayList<UnorderedPair<Integer>> graph = new ArrayList<>();

        // Create a completely connected undirected network
        for (int i = 0; i < numberOfNodes;  ++i )
            for (int j = i+1; j < numberOfNodes; ++j )
                graph.add(new UnorderedPair<>(i,j));

        return graph;
    }


    /**
     * Tests if the parameter set is valid
     * TODO: Check if sufficient
     * @param numberOfNodes number of nodes present in the completely connected network
     * @return true if the parameter set is valid
     */
    protected boolean isValidParameterSet(int numberOfNodes) {
        return numberOfNodes >= 2;
    }

    @Override
    protected boolean isValidDistribution(DegreeDistribution distribution) {
        throw new UnsupportedOperationException("Please change the class structure to reflect parameters necessary for this model");
    }

    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges(DegreeDistribution distribution) {
        throw new UnsupportedOperationException("Please change the class structure to reflect parameters necessary for this model");
    }
}
