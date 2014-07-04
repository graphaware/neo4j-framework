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

    /**
     * The final algorithm has a switch from sparse ER graph to dense ER graph generator.
     * The sparse algorithm is based on trial-correction method as suggested in the paper
     * cited below. This is extremelly inefficient for nearly-complete graphs. The dense
     * algorithm (written by myself) is based on careful avoiding edge indices in the
     * selection. There might be some tweaks possible for this approach as well, as at the
     * present stage a PriorityQueue is used and iterated over for the edge label avoidance.
     *
     * The switch allows to generate even complete graphs (eg. (V, E) = (20, 190) in a
     * reasonable time. The switch is turned on to dense graph generator for the case when
     * number of edges requested is a half of total possible edges to be generated.
     *
     * @param config to base the generation on.
     * @return list of edges in the network.
     */
    protected  List<? extends  SameTypePair<Integer>> doGenerateEdges(ErdosRenyiConfig config) {
        if (4*config.getNumberOfEdges() > config.getNumberOfNodes()*(config.getNumberOfNodes()-1) ) {
            return doGenerateEdgesFaster(config); // Make sure to avoid edges (this takes reasonable time on my system only up till ~ 100000)
        } else {
            return doGenerateEdgesSimpler(config); // Be more heuristic (pajek implementation using HashSet).
        }
    }



    /**
     * Improved implementation of Erdos-Renyi generator based on bijection from
     * edge labels to edge realisations. Works very well for large number of nodes,
     * but is slow with increasing number of edges. Best for denser networks, with
     * a clear giant component.
     *
     * TODO: Remove the bijection iteration and optimise duplicity test?
     *       (effectivelly hashing)
     *
     * @param config configuration of the ER model
     * @return edge list
     */
    protected List<? extends SameTypePair<Integer>> doGenerateEdgesFaster(ErdosRenyiConfig config) {
        int numberOfNodes = config.getNumberOfNodes();
        int numberOfEdges = config.getNumberOfEdges();

        long maxEdges = numberOfNodes * (numberOfNodes - 1) / 2; // must be long, as numberOfNodes^2 can be huge

        LinkedList<UnorderedPair<Integer>> edges = new LinkedList<>();
        PriorityQueue<Long> omitList  = new PriorityQueue<>(); // edges to be omited. TODO: Isn't it more efficient to implement this with HashSet?
        RandomIndexChoice indexChoice = new RandomIndexChoice(); // Index choices with omits

        for (int e = 0; e < numberOfEdges; ++e) {
            long choice = indexChoice.randomIndexChoice(maxEdges, omitList); // must be long to accom. rande of maxEdges
            omitList.add(choice);
            UnorderedPair<Integer> edge = indexToEdgeBijection(choice, numberOfNodes);
            edges.add(edge); // Add the newly created edge (guaranteed unique)
        }

        return edges;
    }

    /**
     * TODO: Accept set on the output? (since the graph is simple)
     *
     * This algorithm is implemented as recommended in
     *
     * Efficient generation of large random networks
     * by Vladimir Batagelj and Ulrik Brandes
     *
     * PHYSICAL REVIEW E 71, 036113, 2005
     *
     * and relies on excellent hashing performance of Java
     * implementation of HashSet.
     *
     * @param config
     * @return edge list
     */
    protected List<? extends SameTypePair<Integer>> doGenerateEdgesSimpler(ErdosRenyiConfig config) {
        // Simplest possible thing to do, believing that hashing algorithm of Java is efficient
        int numberOfNodes = config.getNumberOfNodes();
        int numberOfEdges = config.getNumberOfEdges();
        int origin;
        int target;

        RandomIndexChoice indexChoice = new RandomIndexChoice();
        HashSet<UnorderedPair<Integer>> edges = new HashSet<>();

        for (int e = 0; e < numberOfEdges; ++e) {
            // Generate a new edge, until you've generated a unique one.
            while(true) {
                origin = indexChoice.randomIndexChoice(numberOfNodes);
                target = indexChoice.randomIndexChoice(numberOfNodes, origin);

                UnorderedPair<Integer> candidate = new UnorderedPair<>(origin, target);

                if (! edges.contains(candidate)) {
                    edges.add(candidate);
                    break;
                }
            }
        }

        return new ArrayList<>(edges); // TODO: is there a way to hash the array itself, or accept set on the output?
    }


    /**
     * Maps the edge list to edges.
     * TODO: The iteration over buckets is not optimal. It would be cool if some simple mathematical formula was behind this.
     *       (at the present stage I wasn't able to find any)
     *
     * Note that long indices have to be used to label the edges, since
     * there are numberOfNodes*(numberOfNodes-1) indices available. This
     * is beyond range of int for networks of size above ~ 1 000 000
     *
     * @param index
     * @return an edge based on its unique label
     */
    private UnorderedPair<Integer> indexToEdgeBijection(long index, int numberOfNodes) {

        // Bijection from edge label to realisation seems to be the bottleneck!
        long cummulative = 0;
        int remainder = 0;
        int j;
        for (j = 0; j < numberOfNodes - 1; ++j) { // how to avoid this loop ?
            cummulative += numberOfNodes - j - 1;
            if (cummulative > index) {
                remainder = (int) (index - cummulative + numberOfNodes - j);
                break; // found the correct j
            }
        }

        return new UnorderedPair<>(j, remainder + j);
    }

    /**
     * Legacy method for generating edges of an Erdos-Renyi graph.
     * @param config
     * @return edge list
     *
     * @deprecated
     */
    @Deprecated
    protected List<? extends SameTypePair<Integer>> doGenerateEdgesLegacy(ErdosRenyiConfig config) {
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

}
