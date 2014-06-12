/**
 * EXPERIMENTAL
 * Note that this package is experimental and being developed
 */

package com.graphaware.neo4j.configurationmodel;

import java.util.ArrayList;

/**
 *
 * @author Vojtech Havlicek (Graphaware)
 */
public interface ConfigurationModelGenerator {

    /**
     *
     * @param distribution
     * @return
     * @throws InvalidDistributionException
     */
    public void generateGraph(ArrayList<Integer> distribution) throws InvalidDistributionException;
     
    public boolean isValidDistribution(ArrayList<Integer> distribution);
}
