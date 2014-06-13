/**
 * EXPERIMENTAL
 */
package com.graphaware.neo4j.configurationmodel;

import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Collections;  
import static java.util.Collections.shuffle;
import static java.util.Collections.sort;
import java.util.Comparator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.Pair;

/**
 * To keep track of indices while shuffling
 * @author Vojtech Havlicek (Graphaware)
 */
class SimpleNode 
{
    public int degree;
    public int index;
    public SimpleNode(int degree, int index) {
        this.degree = degree;
        this.index = index;
    }
};

/**
 * Sorts the simpleNodes according to the degree in
 * descending order.
 * @author Vojtech Havlicek (Graphaware)
 */
class SimpleNodeComparator implements Comparator<SimpleNode> {

    @Override
    public int compare(SimpleNode o1, SimpleNode o2) {
        if(o1.degree < o2.degree)
            return 1;
        else if (o1.degree == o2.degree)
            return 0;
        else
            return -1;
    }
    
};

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
        
        System.out.println("distribution after validity check: " + distribution);
        ArrayList<Pair<Integer,Integer>> pairs = new ArrayList<>();
        
        /**
         * Copy distribution into ArrayList of nodes
         */
        int k = 0;
        ArrayList<SimpleNode> nodes = new ArrayList<>();
        for (int degree: distribution) {
            nodes.add(new SimpleNode(degree, k));
            k++;
        }
        
        Comparator comparator = new SimpleNodeComparator();
        
        if (!isValidDistribution(nodes)) 
            throw new InvalidDistributionException("The degree distribution supplied doesn't satisfy Erdos-Galai condition and can be used to generate a simple graph");
        
        
        /**
         * 
         */
        
        while (!nodes.isEmpty()) {
            SimpleNode first = nodes.remove(0);
            
            ArrayList<SimpleNode> temp = new ArrayList<>();
            ArrayList<Pair<Integer,Integer>> tempPairs = new ArrayList<>();
                
            do {
                temp = nodes;
                shuffle(temp);
                
                // choose randomly "first nodes" and connect
                for (int q = 0; q < first.degree; ++q) {
                    // iterate over the reservoir and sample, 
                    // the graph is realizable (tested initially)
                    temp.get(q).degree --;
                    
                    System.out.println(tempPairs.toString() + " " + temp.toString());
                    System.out.println("temp.get(q).index: " + temp.get(q).index + " " + first.index);
               
                    tempPairs.add(Pair.of(temp.get(q).index, first.index));
                     
                    if (temp.get(q).degree == 0) // quite a hack
                        temp.set(q, null);
                    
                    temp.removeAll(Collections.singleton(null));
                } 
            }while(!isValidDistribution(temp));
            
            pairs.addAll(tempPairs);
            nodes = temp;
            sort(nodes, comparator);
        }

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
    private boolean isValidDistribution(ArrayList<SimpleNode> distribution) {
        int L = distribution.size();
        int degreeSum = 0;           // Has to be even by the handshaking lemma

        for (SimpleNode node : distribution) {
            degreeSum += node.degree;
        }

        if (degreeSum % 2 != 0) {
            return false;
        }

        sort(distribution, new SimpleNodeComparator());

        for (int k = 1; k < L; ++k) {
            int sum = 0;
            for (int i = 0; i < k; ++i) {
                sum += distribution.get(i).degree;
            }

            int comp = 0;
            for (int j = k; j < L; ++j) {
                comp += min(k, distribution.get(j).degree);
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
