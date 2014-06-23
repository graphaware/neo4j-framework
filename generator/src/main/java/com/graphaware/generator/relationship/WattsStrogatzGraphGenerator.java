package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.distribution.DegreeDistribution;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Random;

/**
 * Watts-Strogatz model implementation
 */
public class WattsStrogatzGraphGenerator extends BaseRelationshipGenerator {


    @Override
    protected boolean isValidDistribution(DegreeDistribution distribution) {
        throw new UnsupportedOperationException("Distribution argument not accepted in Watts-Strogatz");
    }

    /* TODO: rebase the superclass so it allows for generation of random graphs as well?
       Comments: The null-model is characterised by a degree distribution specified at the input,
                 but for some other useful models (Erdos-Reyni, Barabasi-Albert, Watts-Strogatz),
                 I need just to call doGenerateEdges without any argument. I am not sure about the
                 structure og the framework itself, but would like to keep the base class as
                 flexible and simple as possible.
    */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges(DegreeDistribution distribution) {
        throw new UnsupportedOperationException("Distribution argument not accepted in Watts-Strogatz");
    }

    /**
     * Generates a ring and performs rewiring on the network. This creates
     * a small-world network with high clustering coefficients (ie. there
     * are a lot of triangles present in the network, but the diameter
     * scales as ln(N)). Good choice for modelling simple social network
     * relationships (although hubs are not present).
     *
     * TODO: find a way to control the strength of rewiring in the model
     * @return
     */
    public List<UnorderedPair<Integer>> doGenerateEdges(int numberOfNodes, double meanDegree, double beta) {

        Random random = new Random();
        ArrayList<UnorderedPair<Integer>> ring = new ArrayList<>(numberOfNodes);

        // Create a ring network
        int length = numberOfNodes;
        // TODO: is it worth to replace the hardcoded integer loops with iterators?
        for (int i = 0; i < length; ++i ) {
            for (int j = i + 1; j <= i + meanDegree/2; ++j ) {
                int friend = j % length;
                ring.add(new UnorderedPair<>(i, friend));
            }
        }

        /* Rewire edges with probability beta
           TODO: does the Pair class have to be immutable?
           At the moment, I am hacking my way around a bit here, due to constraints
           enforced by the class structure. There is a room for improvement as this
           implementation is not the most effective one.

           Also, the wiring stops when the algorithm rewires to a non-graphical set of
           edges. Unconnected components may appear in the graph due to rewiring.
           TODO: avoid the algorithm getting stuck during the rewiring.

           Works, but slow and hacked.
        */
        for (ListIterator<UnorderedPair<Integer>> it = ring.listIterator(); it.hasNext();) {
            int index = it.nextIndex(); // index
            UnorderedPair<Integer> edge = it.next(); // get the edge present in the iterator

            // if should rewire, iterate until a new rewiring is found
            if(random.nextDouble() <= beta ) {
                for (int i = 0; i < 10 ; ++ i) { // TODO: make this to be tested for graphicality
                    int choice = random.nextDouble() > .5 ? edge.first() : edge.second(); // select first/second at random

                    int trial = (int) Math.floor(random.nextDouble() * (length-1));       // skip one node
                    int partner = trial < choice ? trial : trial + 1;                     // avoid self-loops

                    // Some test based on Havel-Hakimi condition ???
                    UnorderedPair<Integer> trialPair = new UnorderedPair<>(choice, partner);
                    //System.out.println(trialPair);

                    // Does the ring contain trial pair ???
                    if (ring.contains(trialPair)) {
                        //continue;
                    } else {
                       // System.out.println("Yop");
                        ring.set(index, trialPair); // replace the pair
                        // System.out.println(ring);
                        break;
                    }
                }
                //System.out.println("Failed to rewire (rewired to non-graphical distribution)");
            }
        }

        // Rewire with probability beta
        // for (int node = 0; node < length-1; ++node) {
        //     if(random.nextDouble() <= beta) {
                 /* Rewire: select yode at random and rewire, if the
                  * new wiring is valid (no multiedges, selfloops) */
        //         double maxRnd = 0.0;
        //         int target = 0;
        //         for (int yode = node + 1; yode < length; ++yode) {
        //               if random.nextDouble();
        //       }
        //   }
        // }
        return ring;
    }

    /**
     * Returns true if the parameter is valid. Checks if number of edges is integer
     * and the beta control parameter is valid.
     * @param meanDegree
     * @param beta
     * @return
     */
    protected boolean isValidParameterSet(double meanDegree, double beta) {
        if (meanDegree%2 != 0 || meanDegree < 3) // Do not accept too small mean degrees
            return false;

        return (0 <= beta && beta <= 1);
    }
}
