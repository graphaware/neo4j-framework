package com.graphaware.crawler.internal;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.crawler.api.Context;
import com.graphaware.crawler.api.ThingThatGetsCalledWhenWeFindSomething;

/**
 * Implementation of {@link PerpetualGraphCrawler} that uses a simple recursive algorithm to visit each node in the graph,
 * starting from an arbitrary start node.
 */
public class SimpleRecursiveGraphCrawler implements PerpetualGraphCrawler {

	private static final Logger LOG = Logger.getLogger(SimpleRecursiveGraphCrawler.class);

	private InclusionStrategy<? super Node> nodeInclusionStrategy;
	private InclusionStrategy<? super Relationship> relationshipInclusionStrategy;
	private ThingThatGetsCalledWhenWeFindSomething handler;

	@Override
	public void setNodeInclusionStrategy(InclusionStrategy<? super Node> nodeInclusionStrategy) {
		this.nodeInclusionStrategy = nodeInclusionStrategy;
	}

	@Override
	public void setRelationshipInclusionStrategy(InclusionStrategy<? super Relationship> relationshipInclusionStrategy) {
		this.relationshipInclusionStrategy = relationshipInclusionStrategy;
	}

	@Override
	public void addInclusionHandler(ThingThatGetsCalledWhenWeFindSomething inclusionHandler) {
		this.handler = inclusionHandler;
	}

	@Override
	public void startCrawling(GraphDatabaseService databaseService) {
		if (this.nodeInclusionStrategy == null) {
			this.nodeInclusionStrategy = IncludeAllNodes.getInstance();
		}
		if (this.relationshipInclusionStrategy == null) {
			this.relationshipInclusionStrategy = IncludeAllRelationships.getInstance();
		}

		/* I reckon we will have to start a new transaction for each iteration of the algorithm, otherwise we won't
		 * be able to pick up changes to the graph, assuming transactions worth the way I think they work, that is.
		 *
		 * Probably need to do a bit of research on Neo4j transaction properties...
		 * Points to note:
		 * - transactions are flattened when nested (i.e., all within the scope of the top-level transaction)
		 * - transactions are thread-bound, which could be interesting if we run this offline
		 * 		I think this implies that we have to start/end run each tx within the converging thread
		 * 		In other words, I'm advocating starting a new transaction for each iteration of the algorithm
		 */
		try (Transaction transaction = databaseService.beginTx()) {
			Node arbitraryStartNode = GlobalGraphOperations.at(databaseService).getAllNodes().iterator().next();
			// TODO: actually make this crawl perpetually
			crawl(arbitraryStartNode, 9, 0, null);

			transaction.success(); // I reckon we want this trans'n to be read-only, but I don't know whether that's possible
		}
	}

	private void crawl(Node startNode, int maxDepth, int currentDepth, Relationship howDidIGetHere) {
		if (currentDepth > maxDepth || startNode.getDegree() == 0) {
			return;
		}

		debug("Visiting node: " + startNode.getProperty("name") + " with " + startNode.getDegree() + " relationships", currentDepth);
		if (this.nodeInclusionStrategy.include(startNode)) {
			this.handler.doSomeStuff(new Context(startNode, howDidIGetHere));
		}

		for (Iterator<Relationship> it = startNode.getRelationships(Direction.BOTH).iterator(); it.hasNext();) {
			Relationship relationship = it.next();
			if (!relationship.equals(howDidIGetHere) && this.relationshipInclusionStrategy.include(relationship)) {
				debug("Following relationship: " + relationship.getType(), currentDepth);
				crawl(relationship.getOtherNode(startNode), maxDepth, currentDepth + 1, relationship);
			}
		}

		debug("Done with: " + startNode.getProperty("name"), currentDepth);
	}

	private static void debug(String message, int currentDepth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < currentDepth; i++) {
			sb.append('*');
		}
		sb.append(' ').append(message);
		LOG.debug(sb.toString());
	}

}
