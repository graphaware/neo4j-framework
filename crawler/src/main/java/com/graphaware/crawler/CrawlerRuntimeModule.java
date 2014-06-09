package com.graphaware.crawler;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.crawler.api.ThingThatGetsCalledWhenWeFindSomething;
import com.graphaware.crawler.internal.PerpetualGraphCrawler;
import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;

/**
 * Module that beavers away in the background while a graph database is running, performing arbitrary offline processing with
 * arbitrary nodes.
 */
public class CrawlerRuntimeModule extends BaseGraphAwareRuntimeModule {

	private final CrawlerModuleConfiguration moduleConfiguration;
	private final ThingThatGetsCalledWhenWeFindSomething inclusionHandler;

	/**
	 * Constructs a new {@link CrawlerRuntimeModule} based on the given arguments.
	 *
	 * @param moduleId The ID by which to uniquely identify this module
	 * @param configuration The {@link CrawlerModuleConfiguration} containing the information about how the graph should be
	 *        crawled
	 * @param inclusionHandler The {@link ThingThatGetsCalledWhenWeFindSomething}
	 */
	public CrawlerRuntimeModule(String moduleId, CrawlerModuleConfiguration configuration,
			ThingThatGetsCalledWhenWeFindSomething inclusionHandler) {
		super(moduleId);
		this.moduleConfiguration = configuration;
		this.inclusionHandler = inclusionHandler;
	}

	@Override
	public CrawlerModuleConfiguration getConfiguration() {
		return this.moduleConfiguration;
	}

	@Override
	public void initialize(GraphDatabaseService database) {
		/*
		 * Here, I reckon we will have to set up the thread to crawl the database and allow the consumer of the framework to
		 * choose a NodeSelectionStrategy.  In fact, should it be InclusionStrategy?
		 *
		 * We also need to decide on the throttling here and set that up along with the iterative algorithm for crawling the
		 * graph.  This graph crawling implementation is different from the one used to select the nodes, although the node
		 * inclusion strategy will probably be utilised by the crawler.
		 *
		 * Actually, are these inclusion strategies even necessary?  Couldn't we just have the handler decide if it's interested
		 * or not?  I know there's a separation of concerns here but is it over-engineering?
		 */
		PerpetualGraphCrawler graphCrawler = this.moduleConfiguration.getPerpetualGraphCrawler();
		graphCrawler.setNodeInclusionStrategy(this.moduleConfiguration.getInclusionStrategies().getNodeInclusionStrategy());
		graphCrawler.setRelationshipInclusionStrategy(this.moduleConfiguration.getInclusionStrategies().getRelationshipInclusionStrategy());
		graphCrawler.addInclusionHandler(this.inclusionHandler);
		graphCrawler.startCrawling(database);
	}

	@Override
	public void beforeCommit(ImprovedTransactionData transactionData) {
		/*
		 * so, this is the point at which we're notified of changes, but probably isn't the point at which we hook into the
		 * database for traversal purposes.
		 *
		 * Indeed, given that I don't think I want to override this means I'd question extending the base class.
		 */
	}

}
