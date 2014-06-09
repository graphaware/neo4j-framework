

package com.graphaware.crawler.algo;

import java.util.concurrent.Callable;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Vojtech Havlicek (Graphaware)
 */
public abstract class CrawlerAlgorithm {
    
    protected Configuration config;
    protected Iterable<Node> nodes;
    
     /**
     * @param config
     * @param nodes
     */
    public CrawlerAlgorithm(Configuration config,
                            Iterable<Node> nodes) 
    {
        this.config = config;
        this.nodes = nodes;
    };
    
    /**
     * @param config
     * @param nodes
     * @param callback
     */
    public CrawlerAlgorithm(Configuration config, 
                            Iterable<Node> nodes, 
                            Callable callback)
    {
        this(config, nodes);
    };
    
    /**
     * This is used to call the algorithm.
     */
    public abstract void iterate();
    
    
    
    
    
}
