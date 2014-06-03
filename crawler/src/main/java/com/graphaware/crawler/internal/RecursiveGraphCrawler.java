package com.graphaware.crawler.internal;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Implementation of {@link GraphCrawler} that uses a simple recursive algorithm to visit each node in the graph, starting from
 * an arbitrary start node.
 */
public class RecursiveGraphCrawler implements GraphCrawler {

	@Override
	public void startCrawling(GraphDatabaseService databaseService) {
		try (Transaction transaction = databaseService.beginTx()) {
			Iterator<Node> iterator = GlobalGraphOperations.at(databaseService).getAllNodes().iterator();
			Node arbitraryStartNode = iterator.next();
			crawl(arbitraryStartNode, 9, 0, null);

			transaction.success(); // I reckon we want this trans'n to be read-only
		}
	}

	void crawl(Node startNode, int maxDepth, int currentDepth, Relationship howDidIGetHere) {
		if (currentDepth > maxDepth || startNode.getDegree() == 0) {
			return;
		}

		templog("Traversing " + startNode.getDegree() + " relationships on node: " + startNode.getProperty("name"), currentDepth);

		// TODO: properly decide what relationship to follow and whether to skip certain nodes
		for (Iterator<Relationship> it = startNode.getRelationships(Direction.BOTH).iterator(); it.hasNext();) {
			Relationship outgoingRelationship = it.next();
			if (!outgoingRelationship.equals(howDidIGetHere)) {
				templog("Following relationship: " + outgoingRelationship.getType(), currentDepth);
				crawl(outgoingRelationship.getOtherNode(startNode), maxDepth, currentDepth + 1, outgoingRelationship);
			}
		}

		templog("Done with: " + startNode.getProperty("name"), currentDepth);
	}

	private static void templog(String message, int currentDepth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < currentDepth; i++) {
			sb.append('*');
		}
		sb.append(' ').append(message);
		System.out.println(sb.toString());
	}

}
