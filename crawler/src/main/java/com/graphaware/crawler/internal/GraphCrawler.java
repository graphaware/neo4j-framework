package com.graphaware.crawler.internal;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.crawler.api.ThingThatGetsCalledWhenWeFindSomething;

/**
 * Repeatedly crawls the entire graph, picking out certain parts of it for special attention as discerned by its
 * {@link InclusionStrategy}.  If something is selected for inclusion, it is given to a
 * {@link ThingThatGetsCalledWhenWeFindSomething} along with its contextual information for processing.
 */
public interface GraphCrawler {

	// TODO: Remember to make a note about how transactions are handled when you get round to documenting this method
	void startCrawling(GraphDatabaseService databaseService);

}
