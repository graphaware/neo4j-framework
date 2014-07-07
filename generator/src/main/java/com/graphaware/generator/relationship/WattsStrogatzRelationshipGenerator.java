package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.config.DegreeDistribution;
import com.graphaware.generator.config.InvalidConfigException;
import com.graphaware.generator.config.WattsStrogatzConfig;

import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Random;

/**
 * Watts-Strogatz model implementation.
 */
public class WattsStrogatzRelationshipGenerator extends BaseRelationshipGenerator<WattsStrogatzConfig> {

    public WattsStrogatzRelationshipGenerator(WattsStrogatzConfig configuration) {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Generates a ring and performs rewiring on the network. This creates
     * a small-world network with high clustering coefficients (ie. there
     * are a lot of triangles present in the network, but the diameter
     * scales as ln(N)). Good choice for modelling simple social network
     * relationships (although hubs are not present).
     * <p/>
     * TODO: find a way to control the strength of rewiring in the model
     *
     * @return ring - edge list as a list of unordered integer pairs
     */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges() {
        int numberOfNodes = getConfiguration().getNumberOfNodes();
        int meanDegree = getConfiguration().getMeanDegree();
        double beta = getConfiguration().getBeta();

        // Throw warning if no rewiring is possible? Complete graph?
        Random random = new Random();
        ArrayList<SameTypePair<Integer>> ring = new ArrayList<>(numberOfNodes);

        // Create a ring network
        // TODO: is it worth to replace the hardcoded integer loops with iterators?
        for (int i = 0; i < numberOfNodes; ++i) {
            for (int j = i + 1; j <= i + meanDegree / 2; ++j) {
                int friend = j % numberOfNodes;
                ring.add(new UnorderedPair<>(i, friend));
            }
        }

        /* Rewire edges with probability beta.

           TODO: is it possible to somehow avoid the false rewirings withouth the algorithm being stuck?
                 The false rewirings change the desired probability distribution a little bit, but for
                 large enough networks do not matter.

           At the moment, I am hacking my way around a bit here, due to constraints
           enforced by the class structure. There is a room for improvement as this
           implementation is not the most effective one.

           Also, the wiring stops when the algorithm rewires to a non-graphical set of
           edges. Unconnected components may appear in the graph due to rewiring.

           Works, but slow and hacked.
        */
        for (ListIterator<SameTypePair<Integer>> it = ring.listIterator(); it.hasNext(); ) {
            int index = it.nextIndex(); // index
            SameTypePair<Integer> edge = it.next(); // get the edge present in the iterator

            if (random.nextDouble() <= beta) {
                while (true) {
                    // Allow self-rewiring ? (this avoids problems with complete graphs)
                    int choice = random.nextDouble() > .5 ? edge.first() : edge.second(); // select first/second at random
                    int trial = (int) Math.floor(random.nextDouble() * (numberOfNodes - 1));       // skip self node
                    int partner = trial < choice ? trial : trial + 1;                     // avoid self-loops

                    UnorderedPair<Integer> trialPair = new UnorderedPair<>(choice, partner);

                    // Allows for self-rewiring to avoid parasitic cases?
                    // check with original definition of the model
                    if (trialPair.equals(edge) || !ring.contains(trialPair)) {
                        ring.set(index, trialPair); // replace the pair
                        break;
                    }
                }
            }
        }

        return ring;
    }
}
