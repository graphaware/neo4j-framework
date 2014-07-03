package com.graphaware.neo4j.example.pagerank;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Mechanism by which an arbitrary node is selected in the graph.  This is useful for starting a graph walk or making a jump to
 * another node if there aren't any relationships to follow.
 */
public interface RandomNodeSelector {

	/**
	 * @param databaseService The {@link GraphDatabaseService} on which to select a node
	 * @return A {@link Node} in the given graph database
	 */
	Node selectRandomNode(GraphDatabaseService databaseService);

}
