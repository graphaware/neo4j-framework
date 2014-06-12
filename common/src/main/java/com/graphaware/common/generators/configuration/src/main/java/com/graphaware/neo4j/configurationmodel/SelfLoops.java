/**
 * EXPERIMENTAL
 */

package com.graphaware.neo4j.configurationmodel;

import java.util.ArrayList;
import static java.util.Collections.shuffle;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.Pair;

/**
 * A simple-minded generator of graphs based on 
 * a degree distribution. So far, the model 
 * accepts a list of node degrees as an input
 * and creates a graph according to that.
 * 
 * DESCRIPTION OF THE CLASS:
 * For Self looped model, multiple edges and 
 * self-loops are allowed. For a simple graph
 * generator, see ConfigurationModelGenerator
 * 
 * @author Vojtech Havlicek (Graphaware)
 */

public class SelfLoops  implements ConfigurationModelGenerator {
    
    private final GraphDatabaseService database;
    
    /**
     * 
     * @param database
     */
    public SelfLoops(GraphDatabaseService database) {
        this.database = database;
    };

    /**
     * This works well for large graphs which are almost sparse. Multiple edges
     * and self-loops are present, although in large sparse limit, these are 
     * abundant.
     * 
     * @param distribution
     * @throws com.graphaware.neo4j.configurationmodel.InvalidDistributionException
     */
    @Override
    public void generateGraph(ArrayList<Integer> distribution) throws InvalidDistributionException
    {
        // Spread the distribution, 
        // shuffle and form pairs
        if (!isValidDistribution(distribution))
            throw new InvalidDistributionException("Invalid distribution supplied. Check that the sum of degrees is even");
        
        ArrayList<Integer> spread = spreadDistribution(distribution);
        shuffle(spread);
        
        ArrayList<Pair<Integer,Integer>> pairs = new ArrayList<>();
        
        int L = spread.size();
        for (int i = 0; i < L; i += 2) {
            pairs.add(Pair.of(spread.get(i), spread.get(i+1)));
        }
        
        /* Now feed the pairs to Neo4j -  I am using a 
           graph generating code from the test written by
           Adam George (Graphaware) to do this. Please let me know
           if that is ok */
        try (Transaction transaction = this.database.beginTx()) {
            Label personLabel = DynamicLabel.label("Node");
	    RelationshipType relationshipType = DynamicRelationshipType.withName("relto");

	    for (Pair<Integer, Integer> pair : pairs) {
		Node person = findOrCreateNode(personLabel, ""+pair.first());
		Node colleague = findOrCreateNode(personLabel, ""+pair.other());
		person.createRelationshipTo(colleague, relationshipType);
            }

            transaction.success();
        }
    }
    
    /**
     * All valid distributions must contain even number of stubs.
     * @param distribution
     * @return 
     */
    @Override
    public boolean isValidDistribution(ArrayList<Integer> distribution)
    {
        int degreeSum = 0;           // Has to be even by the handshaking lemma
        for(int degree : distribution) {
            degreeSum += degree;
        }
        return (degreeSum%2) == 0;  
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
    
    
    // The following was 
    // Written by Adam George (Graphaware) as a test for 
    // the crawler class. Please let me know if this is OK
    // in the final implementation as well.
    private Node findOrCreateNode(Label label, String name) {
	ResourceIterable<Node> existingNodes = this.database.findNodesByLabelAndProperty(label, "name", name);
	if (existingNodes.iterator().hasNext()) {
		return existingNodes.iterator().next();
	}
	Node newNode = this.database.createNode(label);
	newNode.setProperty("name", name);
	return newNode;
    }
}
