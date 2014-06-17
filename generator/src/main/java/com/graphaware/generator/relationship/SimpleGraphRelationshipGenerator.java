/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.generator.relationship;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.common.util.UnorderedPair;
import com.graphaware.generator.distribution.DegreeDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.min;

/**
 * A simple minded {@link RelationshipGenerator} based on a {@link DegreeDistribution}
 * <p/>
 * Please note that the distribution of randomly generated graphs isn't exactly uniform (see the paper below)
 * <p/>
 * Uses Blitzstein-Diaconis algorithm Ref:
 * <p/>
 * A SEQUENTIAL IMPORTANCE SAMPLING ALGORITHM FOR GENERATING RANDOM GRAPHS WITH PRESCRIBED DEGREES
 * By Joseph Blitzstein and Persi Diaconis (Stanford University). (Harvard, June 2006)
 */
public class SimpleGraphRelationshipGenerator extends BaseRelationshipGenerator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isValidDistribution(DegreeDistribution distribution) {
        return passesErdosGallaiTest(distribution); //can be swapped for Havel-Hakimi
    }

    /**
     * {@inheritDoc}
     *
     * Returns an edge-set corresponding to a randomly chosen simple graph.
     */
    @Override
    protected List<SameTypePair<Integer>> doGenerateEdges(DegreeDistribution distribution) {
        List<SameTypePair<Integer>> edges = new ArrayList<>();

        while (!distribution.isZeroList()) {
            int length = distribution.size();
            int index = 0;
            int min = Integer.MAX_VALUE;

            // find minimal nonzero element
            for (int i = 0; i < distribution.size(); ++i) {
                int elem = distribution.get(i);
                if (elem != 0 && elem < min) {
                    min = elem;
                    index = i;
                }
            }

            // Obtain a candidate list:
            while (true) {
                DegreeDistribution temp = distribution.duplicate();

                // TODO : this should be proportional to degree to
                // make the random graph distribution as uniform as possible
                int rnd = (int) Math.floor(Math.random() * (length - 1)); // choose an index from one elem. less range. OK
                int candidateIndex = rnd >= index ? rnd + 1 : rnd;       // skip index. OK

                SameTypePair<Integer> edgeCandidate = new UnorderedPair<>(candidateIndex, index);

                /**
                 * Improve this one, check if edge has already been added.
                 */
                boolean skip = false;
                for (SameTypePair<Integer> edge : edges) {
                    if (edge.equals(edgeCandidate)) {
                        skip = true;
                        break;
                    }
                }

                if (skip) {
                    continue;
                }

                /**
                 * Prepare the candidate set and test if it is graphical
                 */
                temp.decrease(index);
                temp.decrease(candidateIndex);

                if (isValidDistribution(temp)) { // use Erdos-Galai test, since it doesn't sort the entries
                    distribution = temp;              // assign temp to distribution
                    edges.add(edgeCandidate);         // edge is allowed, add it.
                    break;
                }
            }
        }
        return edges;
    }

    /**
     * All valid distributions must be graphical. This is tested using Erdos-Gallai condition on degree distribution
     * graphicality. (see Blitzstein-Diaconis paper)
     *
     * @param distribution to test.
     * @return true iff passes.
     */
    public boolean passesErdosGallaiTest(DegreeDistribution distribution) {
        // Do this in-place instead?
        DegreeDistribution copy = distribution.duplicate();

        int L = copy.size();
        int degreeSum = 0;           // Has to be even by the handshaking lemma

        for (int degree : copy.getDegrees()) {
            if (degree < 0) {
                return false;
            }
            degreeSum += degree;
        }

        if (degreeSum % 2 != 0) {
            return false;
        }

        copy.sort(Collections.<Integer>reverseOrder());
        // Erdos-Gallai test
        for (int k = 1; k < L; ++k) {
            int sum = 0;
            for (int i = 0; i < k; ++i) {
                sum += copy.get(i);
            }

            int comp = 0;
            for (int j = k; j < L; ++j) {
                comp += min(k, copy.get(j));
            }

            if (sum > k * (k - 1) + comp) {
                return false;
            }
        }

        return true;
    }

    /**
     * Use Havel-Hakimi test instead of the Erdos-Gallai condition TODO: Do
     * these in-place?
     *
     * @param distribution to test.
     * @return true iff passes.
     */
    private boolean passesHavelHakimiTest(DegreeDistribution distribution) {
        /* 
         * The test fails if there are less available 
         * nodes to connect to than the degree of lar-
         * gest node.
         */

        DegreeDistribution copy = distribution.duplicate();

        int i = 0, L = 0, first = 0;

        while (L > 0) {   //todo this can't work
            first = copy.get(i);
            L--;

            int j = 1;
            for (int k = 0; k < first; ++k) {
                while (copy.get(j) == 0) {

                    j++;
                    if (j > L) {
                        return false;
                    }
                }

                copy.set(j, copy.get(j) - 1); //todo switch to decrease?
            }

            copy.set(i, 0);
            copy.sort(Collections.<Integer>reverseOrder());
        }
        return true;
    }
}
