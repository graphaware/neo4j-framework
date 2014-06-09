package com.graphaware.crawler.internal;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.crawler.api.ThingThatGetsCalledWhenWeFindSomething;

/**
 * Perpetually crawls the entire graph, picking out certain parts of it for special attention as discerned by its
 * {@link InclusionStrategy}.  If something is selected for inclusion, it is given to a
 * {@link ThingThatGetsCalledWhenWeFindSomething} along with its contextual information for processing.
 */
public interface PerpetualGraphCrawler {

	// TODO: Remember to make a note about how transactions are handled when you get round to documenting this method
	void startCrawling(GraphDatabaseService databaseService);

	/**
	 * Sets the {@link InclusionStrategy} to use for choosing which of the nodes visited during the crawl should be passed onto
	 * the handler.
	 *
	 * @param nodeInclusionStrategy The {@link InclusionStrategy} to use for selecting nodes, which may be <code>null</code> if
	 *        node filtering is not required
	 */
	void setNodeInclusionStrategy(InclusionStrategy<? super Node> nodeInclusionStrategy);

	/**
	 * Sets the {@link InclusionStrategy} to use for choosing which of the relationships from nodes visited during the crawl
	 * should be followed in order traverse the graph.
	 *
	 * @param relationshipInclusionStrategy The {@link InclusionStrategy} to use for selecting relationships, which may be
	 *        <code>null</code> if filtering relationships is not required
	 */
	void setRelationshipInclusionStrategy(InclusionStrategy<? super Relationship> relationshipInclusionStrategy);

	// TODO: Javadoc when you think of a remotely decent name for this method and its parameter type!
	void addInclusionHandler(ThingThatGetsCalledWhenWeFindSomething inclusionHandler);

}
