/**
 * EXPERIMENTAL
 */
package com.graphaware.neo4j.configurationmodel;

import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import static java.util.Collections.sort;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.Pair;

/**
 * A simple minded generator of graphs based on a degree distribution. So far,
 * the model accepts a list of node degrees as an input and creates a graph
 * according to that.
 *
 * Uses Blitzstein-Diaconis algorithm Ref:
 *
 * A SEQUENTIAL IMPORTANCE SAMPLING ALGORITHM FOR GENERATING RANDOM GRAPHS WITH
 * PRESCRIBED DEGREES By Joseph Blitzstein and Persi Diaconis (Stanford
 * University). (Harvard, June 2006)
 *
 * @author Vojtech Havlicek (Graphaware)
 */


public class Simple {

    private final GraphDatabaseService database;

    /**
     * void generator for testing purposes
     */
    public Simple() {
        database = null;
    }

    /**
     *
     * @param database
     */
    public Simple(GraphDatabaseService database) {
        this.database = database;
    }

    ;
    
    /**
     * Implements a simple Havel-Hakimi randomized 
     * generator as described in Blitzstein/Diaconis
     * @param distribution
     * @return
     * @throws com.graphaware.neo4j.configurationmodel.InvalidDistributionException
     */
    public boolean generateGraph(ArrayList<Integer> distribution) throws InvalidDistributionException {
        ArrayList<UnorderedPair<Integer>> edges = new ArrayList<>();
        
        //while(!distribution.isEmpty()) { 
            int length = distribution.size();
            int index  = distribution.indexOf(Collections.min(distribution));
            int degree = distribution.get(index); // choose a (nonzero) minimum
            
            System.out.println(distribution);
            // Obtain a candidate list:
            //while(true){
                ArrayList<Integer> temp = new ArrayList<>(distribution);
                
                // TODO : this should be proportional to degree.
                int rnd =  (int) Math.floor(Math.random()*(length - 1)); // choose an index from one elem. less range. OK
                int candidateIndex = rnd >= index ? rnd + 1 : rnd;       // skip index. OK

                UnorderedPair<Integer> edgeCandidate = new UnorderedPair(candidateIndex, index);

                /**
                 * Improve this one, check if edge has already been added.
                 */
                for(UnorderedPair<Integer> edge : edges)
                    if(edge.equals(edgeCandidate))
                        continue;
                
                /**
                 * Prepare the candidate set and test if it is graphical
                 */
                decrease(temp, index);
                decrease(temp, candidateIndex);
                
                if(isValidDistribution(temp)){
                    distribution = temp; // assign temp to distribution
                    //break;
                }
                System.out.println(temp);
                System.out.println(distribution);
            //}
            
            // candidate list is obtained at this stage.
            
            
            
        //}
        
        // Edge set is know at this place
        return true;
        
        
    }

    /**
     * 
     */
    private boolean isNullArrayList(ArrayList<Integer> distribution) {
        for (int degree : distribution)
            if (degree > 0)
                return false;
        return true;      
    }
    
    /**
     * Fisher-Yates shuffle on part of the array list
     *
     * @param arrList
     * @param from
     * @param to
     */
    private ArrayList<Integer> shufflePartially(ArrayList<Integer> distribution, int from, int to) {

        // Use the Fisher-Yates shuffle
        for (int j = from; j < to; j++) {

            double max = 0.0, rnd;
            int r = 0; // resulting index to be swapped

            for (int q = j; q < to; q++) {
                rnd = Math.random();

                if (rnd > max) {
                    r = q;
                    max = rnd;
                }
            }

            swap(distribution, r, j);

        }
        return distribution;
    }

    /**
     * Reduces element value by 1
     *
     * @param distribution
     * @param index
     */
    private void decrease(ArrayList<Integer> distribution, int index) {
        distribution.set(index, distribution.get(index) - 1);
    }

    /**
     * swaps the two entries at i,j
     *
     * @param distribution
     * @param i
     * @param j
     */
    private void swap(ArrayList<Integer> distribution, int i, int j) {
        int k = distribution.get(i);
        distribution.set(i, distribution.get(j));
        distribution.set(j, k);
    }

    /**
     * Takes distribution indices and spreads them over degree times for each
     * node, to allow for pairing manipulation.
     *
     * @return
     */
    private ArrayList<Integer> spreadDistribution(ArrayList<Integer> distribution) {
        ArrayList<Integer> spread = new ArrayList<>();

        int L = distribution.size();
        for (int k = 0; k < L; ++k) {
            for (int j = 0; j < distribution.get(k); ++j) {
                spread.add(k);
            }
        }

        return spread;
    }

    /**
     * All valid distributions must be graphical. This is tested using
     * Erdos-Gallai condition on degree distribution graphicality. (see
     * Blitzstein-Diaconis paper)
     *
     * @param distribution
     * @return
     */
    private boolean isValidDistribution(ArrayList<Integer> distribution) {
        System.out.println(distribution.toString());
        int L = distribution.size();
        int degreeSum = 0;           // Has to be even by the handshaking lemma

        for (int degree : distribution) {
            degreeSum += degree;
        }

        if (degreeSum % 2 != 0) {
            return false;
        }

        sort(distribution);

        for (int k = 1; k < L; ++k) {
            int sum = 0;
            for (int i = 0; i < k; ++i) {
                sum += distribution.get(i);
            }

            int comp = 0;
            for (int j = k; j < L; ++j) {
                comp += min(k, distribution.get(j));
            }

            if (sum > k * (k - 1) + comp) {
                return false;
            }
        }

        return true;
    }

    /**
     * Use Havel-Hakimi test instead of the Erdos-Gallai condition
     *
     * @param distribution
     * @param havelHakimi
     * @return
     */
    private boolean isValidDistribution(ArrayList<Integer> distribution, boolean havelHakimi) {
        /* 
         * The test fails if there are less available 
         * nodes to connect to than the degree of lar-
         * gest node.
         */

        int i = 0, L = 0, first = 0;

        while (L > 0) {
            first = distribution.get(i);
            L--;

            int j = 1;
            for (int k = 0; k < first; ++k) {
                while (distribution.get(j) == 0) {
                    j++;
                    if (j > L) {
                        return false;
                    }
                }

                distribution.set(j, distribution.get(j) - 1);
            }

            distribution.set(i, 0);
            sort(distribution, Collections.reverseOrder());
        }

        System.out.println("Supplied degree distribution is graphical!");
        return true;
    }

    /**
     * Ready for another generators
     *
     * @return
     */
    private double random() {
        return Math.random();
    }

}
