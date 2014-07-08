package com.graphaware.example.pagerank;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Mechanism by which an arbitrary node is selected in the graph. This is useful for starting a graph walk or making a
 * jump to another node if there aren't any relationships to follow.
 */
public interface RandomNodeSelector {

    /**
     * @param database in which to select a node.
     * @return A random {@link Node} in the given database.
     */
    Node selectRandomNode(GraphDatabaseService database);
}
