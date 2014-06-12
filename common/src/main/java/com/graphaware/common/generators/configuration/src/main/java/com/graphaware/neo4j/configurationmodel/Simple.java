/**
 * EXPERIMENTAL 
 */

package com.graphaware.neo4j.configurationmodel;

import java.util.ArrayList;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.Pair;

/**
 * A simple minded generator of graphs based on 
 * a degree distribution. So far, the model 
 * accepts a list of node degrees as an input
 * and creates a graph according to that.
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
    };
    
    /**
     *
     * @param distribution
     * @return
     * @throws com.graphaware.neo4j.configurationmodel.InvalidDistributionException
     */
    public boolean generateGraph(ArrayList<Integer> distribution) throws InvalidDistributionException
    {
        if (!isValidDistribution(distribution))
            throw new InvalidDistributionException("Invalid distribution supplied. Check that the sum of degrees is even and less than Nx(N-1) and all degrees are less than N-1 ");
        
        // Naive implementation, optimize this.
        ArrayList<Pair<Integer,Integer>> pairs = new ArrayList<>();
        
        // Idea: 
        // partition the distribution into two halves with 
        // two equal sums of degrees? - ensures uniquenses of generated pairs
        // 
        // hope it is still unbiased. The equal degree sum will be an invariant
       
        
        
        System.out.println(distribution.toString());
        swap(distribution, 0, 1);
        System.out.println(distribution.toString());
        System.out.println(spreadDistribution(distribution).toString());
        
        ArrayList<Integer> spread = spreadDistribution(distribution);
        
        // distr: 4,1,1,2,4 
        
                   
        
        
        // spread: 0,0,0,0,1,2,3,3,4,4,4,4
        // 
        // 
        
        
        
        
        
        
        
        /*
        System.out.println(distribution);
        for(int i = 0; i < distribution.size(); ++i)
        {
            int degree = distribution.get(i);
            for (int k = 0; k < degree; ++k)
            {
                double max = 0.0;
                int friend = 0;
                for(int j = i; j < distribution.size(); ++j)
                {
                    if(random() >= max)
                    {
                        if (distribution.get(j) > 0)
                            friend = j;
                    } 
                }
                distribution.set(friend, distribution.get(friend) - 1);
                
                pairs.add(Pair.of(i, friend));
            }
            
        }
        */
        
        for(Pair<Integer, Integer> pair: pairs) {
            System.out.println(pair.toString());
        }
        
        return false;
        
    }
    
    /**
     * swaps the two entries at i,j
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
     * Takes distribution indices and spreads them over degree times
     * for each node, to allow for pairing manipulation.
     * @return 
     */
    private ArrayList<Integer> spreadDistribution(ArrayList<Integer> distribution)
    {
        ArrayList<Integer> spread = new ArrayList<>();
        
        int L = distribution.size();
        for(int k = 0; k < L; ++k ) 
            for (int j = 0; j < distribution.get(k); ++j)
                spread.add(k);
        
        return spread;
    }
    
    /**
     * All valid distributions must contain even number of stubs.
     * @param distribution
     * @return 
     */
    private boolean isValidDistribution(ArrayList<Integer> distribution)
    {
        int N = distribution.size(); // Number of elements in the list
        int degreeSum = 0;           // Has to be even by the handshaking lemma
        
        for(int degree : distribution) {
            degreeSum += degree;
            
            if (degree > N-1)
                return false;
        }
        
        /**
         * If there are more edges than tuples, 
         * self loops would have to be present
         */
        if (degreeSum > N*(N-1))
            return false;
        
        return (degreeSum%2) == 0;  
    }
    
    /**
     * Ready for another generators
     * @return 
     */
    private double random()
    {
        return Math.random();
    }
    
    
    
}
