package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.config.DegreeDistribution;
import com.graphaware.generator.config.InvalidConfigException;
import com.graphaware.generator.config.NumberOfNodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a complete graph. Used for the core graph in
 * Barabasi-Albert network generator
 * <p/>
 * TODO: change the base-class such that it reflects the different parameter sets needed for the network
 */
public class CompleteGraphRelationshipGenerator extends BaseRelationshipGenerator<NumberOfNodes> {

    /**
     * {@inheritDoc}
     * <p/>
     * Generates a completely connected (undirected) graph.
     *
     * @param config number of nodes present in the completely connected network
     * @return graph
     */
    @Override
    protected List<UnorderedPair<Integer>> doGenerateEdges(NumberOfNodes config) {
        ArrayList<UnorderedPair<Integer>> graph = new ArrayList<>();

        // Create a completely connected undirected network
        for (int i = 0; i < config.getNumberOfNodes(); ++i)
            for (int j = i + 1; j < config.getNumberOfNodes(); ++j)
                graph.add(new UnorderedPair<>(i, j));

        return graph;
    }
}
