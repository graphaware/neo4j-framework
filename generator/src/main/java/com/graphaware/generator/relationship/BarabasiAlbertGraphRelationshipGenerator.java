package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.config.BarabasiAlbertConfig;
import com.graphaware.generator.config.NumberOfNodes;
import com.graphaware.generator.utils.WeightedReservoirSampler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Barabasi-Albert model implementation. The model is
 * appropriate for networks reflecting cummulative
 * advantage (rich get richer).
 */
public class BarabasiAlbertGraphRelationshipGenerator extends BaseRelationshipGenerator<BarabasiAlbertConfig> {

    public BarabasiAlbertGraphRelationshipGenerator(BarabasiAlbertConfig configuration) {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Generates a network according to Barabasi-Albert preferential attachment model.
     * Each newly added node has a probability weighted by the node degree to be attached.
     * <p/>
     * Since BA references (Newmann, Barabasi-Albert) do not define strict conditions on initial state of the
     * model, completelly connected network is used to start up the algorithm
     */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges() {
        int edgesPerNewNode = getConfiguration().getEdgesPerNewNode();

        // Create a completely connected network
        CompleteGraphRelationshipGenerator coreGenerator = new CompleteGraphRelationshipGenerator(new NumberOfNodes(edgesPerNewNode + 1));
        List<SameTypePair<Integer>> edges = coreGenerator.doGenerateEdges();

        // Degree list of the network
        ArrayList<Integer> degrees = new ArrayList<>();

        for (int k = 0; k < edgesPerNewNode + 1; ++k) {
            degrees.add(0);
            for (int l = 0; l < edgesPerNewNode; ++l) {
                degrees.set(k, degrees.get(k) + 1);
            }
        }

        WeightedReservoirSampler reservoirSampler = new WeightedReservoirSampler();

        // Preferentially attach other nodes
        for (int node = edgesPerNewNode + 1; node < getConfiguration().getNumberOfNodes(); ++node) {
            Set<Integer> omit = new HashSet<>();

            for (int edge = 0; edge < edgesPerNewNode; ++edge) {
                int target = reservoirSampler.randomIndexChoice(degrees, omit); // find a target
                degrees.set(target, degrees.get(target) + 1); // Any better way of incrementing an index in ArrayList?
                omit.add(target); // Add the target to omit list (and avoid multiedges)
                edges.add(new UnorderedPair<>(target, node)); // Add the edge
                degrees.add(node, edgesPerNewNode); // Add the newly added node to the degree set
            }
        }

        return edges;
    }
}
