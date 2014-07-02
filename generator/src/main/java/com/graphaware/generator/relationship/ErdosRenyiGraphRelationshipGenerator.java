package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.config.ErdosRenyiConfig;
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
            LinkedList<Integer> fromIndices = new LinkedList<>(); // store the from indices which have been already tested for all node connections
            boolean edgeFound = false;

            while(true) {
                LinkedList<Integer> omitIndices = new LinkedList<>();

                int from = reservoirSampler.randomIndexChoice(numberOfNodes, fromIndices);
                fromIndices.add(from);
                omitIndices.add(from);

                while (true) {
                    int to = reservoirSampler.randomIndexChoice(numberOfNodes, omitIndices);
                    omitIndices.add(to);

                    UnorderedPair<Integer> edge = new UnorderedPair<>(from, to);

                    if (!edges.contains(edge)) {
                        edges.add(edge);
                        edgeFound = true;
                        break;
                    }

                    if(omitIndices.size() == numberOfNodes)
                        break; // broken without adding edge, skip to new from


                } // If not succeeded, add from to the omit list and select a new one

                if (edgeFound)
                    break; // Break the outer loop as well if edge was found
            }
        }

        System.out.println(edges.toString());
        return edges;
    }
}
