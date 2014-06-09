package com.graphaware.crawler;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.crawler.internal.SimpleRecursiveGraphCrawler;

/**
 * The default {@link CrawlerModuleConfiguration} that can be used immediately once instantiated.
 */
public class DefaultCrawlerModuleConfiguration extends CustomCrawlerModuleConfiguration {

	/**
	 * Constructs a {@link DefaultCrawlerModuleConfiguration} containing, naturally, the default configuration for such a module.
	 */
	public DefaultCrawlerModuleConfiguration() {
		super(IncludeAllNodes.getInstance(), IncludeAllRelationships.getInstance(), new SimpleRecursiveGraphCrawler());
	}

}
