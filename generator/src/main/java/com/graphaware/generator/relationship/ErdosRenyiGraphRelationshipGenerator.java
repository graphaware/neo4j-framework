package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.config.ErdosRenyiConfig;
import com.graphaware.generator.utils.RandomIndexChoice;
import com.graphaware.generator.utils.ReservoirSampler;

import java.util.*;

/**
 * Implementation of Erdos-Renyi random graphs. These are a basic class of
 * random graphs with exponential cut-off. A phase transition from many
 * components graph to a completely connected graph is present.
 */
public class ErdosRenyiGraphRelationshipGenerator extends BaseRelationshipGenerator<ErdosRenyiConfig> {

    protected List<? extends SameTypePair<Integer>> doGenerateEdges(ErdosRenyiConfig config) {
        int numberOfNodes = config.getNumberOfNodes();
        int numberOfEdges = config.getNumberOfEdges();

        ReservoirSampler reservoirSampler = new ReservoirSampler();
        LinkedList<UnorderedPair<Integer>> edges = new LinkedList<>();

        for (int e = 0; e < numberOfEdges; ++e) {
            PriorityQueue<Integer> fromIndices = new PriorityQueue<>(); // store the from indices which have been already tested for all node connections
            boolean edgeFound = false;

            while (!edgeFound) {
                PriorityQueue<Integer> omitIndices = new PriorityQueue<>();

                int from = reservoirSampler.randomIndexChoice(numberOfNodes, fromIndices);
                fromIndices.add(from);
                omitIndices.add(from);

                while (true) {
                    int to = reservoirSampler.randomIndexChoice(numberOfNodes, omitIndices);
                    omitIndices.add(to);

                    UnorderedPair<Integer> edge = new UnorderedPair<>(from, to);

                    // bottleneck
                    if (!edges.contains(edge)) {
                        edges.add(edge);
                        edgeFound = true;
                        break;
                    }

                    if (omitIndices.size() == numberOfNodes)
                        break; // broken without adding edge, skip to new from


                } // If not succeeded, add from to the omit list and select a new one
            }
        }

        return edges;
    }

    /**
     * EXPERIMENTAL:
     * Improved implementation of Erdis-Renyi generator based on bijection from
     * edge labels to edge realisations. Works very well for large number of nodes,
     * but is slow with increasing number of edges.
     *
     * TODO: Remove the bijection iteration and optimise duplicity test?
     * @param config configuration of the ER model
     * @return edge list
     */
    public List<? extends SameTypePair<Integer>> doGenerateEdgeFaster(ErdosRenyiConfig config) {
        long numberOfNodes = (long) config.getNumberOfNodes();
        long numberOfEdges = (long) config.getNumberOfEdges();

        long maxEdges = numberOfNodes * (numberOfNodes - 1)/2;

        LinkedList<UnorderedPair<Integer>> edges = new LinkedList<>();
        PriorityQueue<Long> omitList = new PriorityQueue<>(); // edges to be omitted
        RandomIndexChoice indexChoice = new RandomIndexChoice();

        for (int e = 0; e < numberOfEdges; ++e) {

            long choice;

            //if (numberOfEdges > maxEdges/2)
            choice = indexChoice.randomIndexChoice(maxEdges, omitList); // ()long) Math.floor(random.nextDouble() * maxEdges); // sampler.randomIndexChoice(maxEdges, omitList);
            //else {
              //  choice = (long) Math.floor(maxEdges * random.nextDouble()); // How to check that the edge has been added already ?
            //}

            omitList.add(choice);

            // Bijection from edge label to realisation seems to be the bottleneck!
            long cum = 0;
            int rem = 0;
            int j;
            for (j = 0; j < numberOfNodes - 1; ++j) { // how to avoid this loop ?
                cum += numberOfNodes - j - 1;
                if (cum > choice) {
                    rem = (int) (choice - cum + numberOfNodes - j);
                    break; // found the correct j
                }
            }

            // Add the newly created edge (guaranteed unique)
            edges.add(new UnorderedPair<>(j, rem + j));
        }

        return  edges;
    }
}
