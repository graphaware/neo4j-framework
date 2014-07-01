package com.graphaware.generator.relationship;

import com.graphaware.generator.utils.WeightedReservoirSampler;
import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.distribution.DegreeDistribution;

import java.util.ArrayList;
import java.util.List;

/**
 * Barabasi-Albert model implementation. The model is
 * appropriate for networks reflecting cummulative
 * advantage (rich get richer).
 */
public class BarabasiAlbertGraphRelationshipGenerator extends BaseRelationshipGenerator {

    @Override
    protected boolean isValidDistribution(DegreeDistribution distribution) {
        throw new UnsupportedOperationException("Please change the class structure to reflect parameters necessary for this model");
    }

    /* TODO: change the superclass so it allows for generation of random graphs
       Comment: As for Watts-Strogatz, this relationship generator requires different
                arguments than those present in BaseRelationshipGenerator class.

                Possibly create two different interfaces - one for configuration
                model, the other for random graphs?
     */

    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges(DegreeDistribution distribution) {
        throw new UnsupportedOperationException("Please change the class structure to reflect parameters necessary for this model");
    }

    /**
     * Generates a network according to Barabasi-Albert preferential attachment model.
     * Each newly added node has a probability weighted by the node degree to be attached.
     *
     * Since BA references (Newmann, Barabasi-Albert) do not define strict conditions on initial state of the
     * model, completelly connected network is used to start up the algorithm
     *
     * @param numberOfNodes number of nodes in the network
     * @param edgesPerNewNode number of edges per newly added node
     * @return edge set
     */
    public List<UnorderedPair<Integer>> doGenerateEdges(int numberOfNodes, int edgesPerNewNode) {

        // Create a completely connected network
        CompleteGraphRelationshipGenerator coreGenerator = new CompleteGraphRelationshipGenerator();
        List<UnorderedPair<Integer>> edges = coreGenerator.doGenerateEdges(edgesPerNewNode+1);

        // Degree list of the network
        ArrayList<Integer> degrees = new ArrayList<>();

        for(int k = 0; k < edgesPerNewNode + 1; ++k) {
            degrees.add(0);
            for (int l = 0; l < edgesPerNewNode; ++l) {
                degrees.set(k, degrees.get(k) + 1);
            }
        }

        WeightedReservoirSampler reservoirSampler = new WeightedReservoirSampler();

        // Preferentially attach other nodes
        for (int node = edgesPerNewNode + 1; node < numberOfNodes; ++ node) {
            List<Integer> omit = new ArrayList<>();

            for (int edge = 0; edge < edgesPerNewNode; ++ edge) {
                int target = reservoirSampler.randomIndexChoice(degrees, omit); // find a target
                degrees.set(target, degrees.get(target) + 1); // Any better way of incrementing an index in ArrayList?
                omit.add(target); // Add the target to omit list (and avoid multiedges)
                edges.add(new UnorderedPair<>(target, node)); // Add the edge
                degrees.add(node, edgesPerNewNode); // Add the newly added node to the degree set
            }
        }

        return edges;
    }

    /**
     * Tests if the parameter set is valid
     * @param numberOfNodes number of nodes in the network
     * @param edgesPerNewNode number of edges per newly added node
     * @return true if the parameter set is valid
     */
    protected boolean isValidParameterSet(int numberOfNodes, int edgesPerNewNode) {
        // Necessary conditions.
        // TODO: Check thoroughly if these are sufficient as well.
        return !(edgesPerNewNode < 2 || edgesPerNewNode + 1 > numberOfNodes);
    }
}
