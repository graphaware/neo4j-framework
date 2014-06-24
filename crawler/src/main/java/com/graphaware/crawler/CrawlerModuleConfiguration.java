package com.graphaware.crawler;

import com.graphaware.crawler.internal.PerpetualGraphCrawler;
import com.graphaware.runtime.config.TxDrivenModuleConfiguration;

/**
 * {@link com.graphaware.runtime.config.TxDrivenModuleConfiguration} through which the crawler module can be configured.
 *
 * @see CrawlerRuntimeModule
 * @see com.graphaware.crawler.bootstrap.CrawlerModuleBootstrapper
 */
public interface CrawlerModuleConfiguration extends TxDrivenModuleConfiguration {

	/**
	 * Retrieves the {@link PerpetualGraphCrawler} implementation to use for crawling the graph.
	 *
	 * @return The {@link PerpetualGraphCrawler} implementation to use, which may not be <code>null</code>
	 */
	PerpetualGraphCrawler getPerpetualGraphCrawler();

}
