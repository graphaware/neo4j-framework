package com.graphaware.crawler;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.BaseGraphAwareRuntimeModule;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;

/**
 * Module that beavers away in the background while a graph database is running, performing arbitrary offline processing with
 * arbitrary nodes.
 */
public class CrawlerRuntimeModule extends BaseGraphAwareRuntimeModule {

	/**
	 * Constructs a new {@link CrawlerRuntimeModule} identified by the given argument.
	 *
	 * @param moduleId The ID by which to uniquely identify this module
	 */
	public CrawlerRuntimeModule(String moduleId) {
		super(moduleId);
	}

	@Override
	public void initialize(GraphDatabaseService database) {
		/*
		 * Here, I reckon we will have to set up the thread to crawl the database and allow the consumer of the framework to
		 * choose a NodeSelectionStrategy. In fact, should I use InclusionStrategy?
		 */
	}

	@Override
	public void beforeCommit(ImprovedTransactionData transactionData) {
		/*
		 * so, this is the point at which we're notified of updates, but probably isn't the point at which we hook into the
		 * database for traversal purposes.
		 *
		 * Indeed, given that I don't think I want to override this means I'd question using the base class.
		 */
	}

}
