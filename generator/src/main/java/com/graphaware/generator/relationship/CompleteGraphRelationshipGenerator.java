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
     *
     * @param configuration number of nodes present in the completely connected network
     */
    public CompleteGraphRelationshipGenerator(NumberOfNodes configuration) {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Generates a completely connected (undirected) graph.
     *
     * @return graph
     */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges() {
        ArrayList<SameTypePair<Integer>> graph = new ArrayList<>();

        // Create a completely connected undirected network
        for (int i = 0; i < getConfiguration().getNumberOfNodes(); ++i)
            for (int j = i + 1; j < getConfiguration().getNumberOfNodes(); ++j)
                graph.add(new UnorderedPair<>(i, j));

        return graph;
    }
}
