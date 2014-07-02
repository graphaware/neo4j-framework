package com.graphaware.neo4j.example.pagerank;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Very simple implementation of {@link RandomNodeSelector} that chooses an arbitrary node from the graph database in O(n) time.
 */
public class HyperJumpRandomNodeSelector implements RandomNodeSelector {

	@Override
	public Node selectRandomNode(GraphDatabaseService databaseService) {
		Iterable<Node> nodes = GlobalGraphOperations.at(databaseService).getAllNodes();
		Node target = null; // again, could be formulated in a better way.

		double max = 0.0;
		for (Node temp : nodes) {
			double rnd = random();
			if (rnd > max) {
				max = rnd;
				target = temp;
			}
		}

		return target;
	}

	private double random() {
        return Math.random();
	}

}
