package com.graphaware.example.module;

import static com.graphaware.common.util.IterableUtils.getSingle;

import java.lang.Exception;
import java.util.Iterator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import static org.neo4j.tooling.GlobalGraphOperations.at;


/**
 * First shot on the PageRank-like algorithm, assuming a 
 * single relationship type and static structure
 * of the network (the model is not dynamic and doesn't 
 * react on network changes). Also assumes a single 
 * strongly connected component only (no hyperjumps)!
 * 
 * Also note that as implemented, we have a local (complex)
 * process rather than a global p.
 * 
 * I assume that Nodes have property NEORANK.
 * 
 * As Michal suggested initially, it would be great to
 * run this continuously, if processing power allows, so
 * that the global rank forms in real time based on local
 * interaction (nearest-neighbour and hyper jumps).
 * 
 * @TODO split implementation and API
 * 
 * @author Vojtech Havlicek (Graphaware)
 */
public class NeoRankAlgorithm {
    
    private final GraphDatabaseService database;
    private Node current;          // currently visited node
    private int normalization = 0; // number of ranks assigned to normalize
    
    /** 
     * Algorithm for NeoRank on static network
     * @param database 
     */
    public NeoRankAlgorithm(GraphDatabaseService database) {
        this.database = database;
        
        try 
        {
            init();
        }catch(Exception e){
           // Whatever, implement this later
        }
    }
    
    /** 
     * Initialise the algorithm (find a start node)
     * @throws java.lang.Exception
     * 
     * @TODO: Define  a EmptyGraphException (I bet there is 
     * something like that present in the framework).
     */
    public final void init() throws Exception {
        // Get the first node in the collection and store it as current
        current = getSingle(at(database).getAllNodes()); 
        
        /* Throw an exception in case no node is present 
         * in the graph */
        if(current == null)
            throw new Exception("No node in the database"); 
    }
    
    /** 
     * Perform a random-walker step on the graph. No 
     * hyperjumps yet.
     * 
     * Call this whenever you want, it will update the system.
     * (I will focus on some theory to get a bound on number
     * of steps needed for convergence of this process, so we 
     * can see how fast we can obtain some meaningful data)
     */
    public void step() 
    {
       /**
        * Proceed to a randomly choosen nearest neighbour
        */
       current = chooseNeighbourAtRandom(current);
       int rank = (int) current.getProperty(PropertyKeys.NEORANK, 0);
       current.setProperty(PropertyKeys.NEORANK, rank+1);
       
       // increment normalization constant to allow to present results consistently 
       normalization ++;  
    } 
    
    /**
     * Chooses a random neighbour of n. Assuming single component
     * graph, all nodes have at least degree 1.
     * 
     * Uses  reservoir sampling 
     * http://en.wikipedia.org/wiki/Reservoir_sampling
     * to get a random guy from the iterable
     * 
     * LCG used so far, I recommend to use Mersenne Twister/RANLUX or similar
     * later on.
     * http://stackoverflow.com/questions/453479/how-good-is-java-util-random
     *
     * @param n
     * @return 
     */
    private Node chooseNeighbourAtRandom(Node n)
    {
        double max =  .0f;
        Relationship edgeChoice = null; // Not entirely kosher, correct later
        Iterable<Relationship> relationships = n.getRelationships();

        for(Iterator<Relationship> it = relationships.iterator(); it.hasNext();)
        {
           Relationship temp = it.next();
           
           double rnd = Math.random();
           if (rnd > max){
               max = rnd;
               edgeChoice = temp;
           }  
        }
        
        return edgeChoice.getEndNode();
    }
    
    
    
    
    
    
}
