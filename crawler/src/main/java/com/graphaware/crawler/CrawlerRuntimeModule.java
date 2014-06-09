package com.graphaware.crawler;

import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.crawler.api.ThingThatGetsCalledWhenWeFindSomething;
import com.graphaware.crawler.internal.NeoRankCrawler;
import com.graphaware.crawler.internal.PerpetualGraphCrawler;
import com.graphaware.crawler.internal.SimpleRecursiveGraphCrawler;
import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Module that beavers away in the background while a graph database is running, 
 * performing arbitrary offline processing with
 * arbitrary nodes.
 */
public class CrawlerRuntimeModule extends BaseGraphAwareRuntimeModule {

	private final InclusionStrategy<Node> nodeInclusionStrategy;
        private final InclusionStrategy<Relationship> relInclusionStrategy;
	private final ThingThatGetsCalledWhenWeFindSomething inclusionHandler;

	/**
	 * Constructs a new {@link CrawlerRuntimeModule} identified by the given argument.
         * TODO: Could you please make some convenient overrides with default strategies 
         *       like include all, etc.
	 *
	 * @param moduleId The ID by which to uniquely identify this module
	 * @param nodeInclusionStrategy The {@link InclusionStrategy} to use for discerning whether we care about a particular node
         * @param relInclusionStrategy
	 * @param inclusionHandler The {@link ThingThatGetsCalledWhenWeFindSomething}
	 */
	public CrawlerRuntimeModule(String moduleId, InclusionStrategy<Node> nodeInclusionStrategy, InclusionStrategy<Relationship> relInclusionStrategy,
			ThingThatGetsCalledWhenWeFindSomething inclusionHandler) {
		super(moduleId);
		this.nodeInclusionStrategy = nodeInclusionStrategy;
                this.relInclusionStrategy = relInclusionStrategy;
		this.inclusionHandler = inclusionHandler;
	}
        /**
         * No handler supplied
         * @param moduleId
         * @param nodeInclusionStrategy
         * @param inclusionHandler
         */
        public CrawlerRuntimeModule(String moduleId, InclusionStrategy<Node> nodeInclusionStrategy, ThingThatGetsCalledWhenWeFindSomething inclusionHandler) {
		super(moduleId);
		this.nodeInclusionStrategy = nodeInclusionStrategy;
                this.relInclusionStrategy = IncludeAllRelationships.getInstance();
                this.inclusionHandler = inclusionHandler;
	}
        
        /**
         * No handler supplied
         * @param moduleId
         * @param nodeInclusionStrategy
         * @param relInclusionStrategy 
         */
        public CrawlerRuntimeModule(String moduleId, InclusionStrategy<Node> nodeInclusionStrategy, InclusionStrategy<Relationship> relInclusionStrategy) {
		super(moduleId);
		this.nodeInclusionStrategy = nodeInclusionStrategy;
                this.relInclusionStrategy = relInclusionStrategy;
                this.inclusionHandler = null;
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
		PerpetualGraphCrawler crawler = new NeoRankCrawler();// new SimpleRecursiveGraphCrawler();
		crawler.setNodeInclusionStrategy(this.nodeInclusionStrategy);
		crawler.setRelationshipInclusionStrategy(this.relInclusionStrategy);
                crawler.addInclusionHandler(this.inclusionHandler);
		crawler.startCrawling(database);
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
