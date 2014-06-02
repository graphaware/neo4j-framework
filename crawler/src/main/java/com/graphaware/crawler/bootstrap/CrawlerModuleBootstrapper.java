package com.graphaware.crawler.bootstrap;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.crawler.CrawlerRuntimeModule;
import com.graphaware.runtime.GraphAwareRuntimeModule;
import com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper;

/**
 * {@link GraphAwareRuntimeModuleBootstrapper} for the module that beavers away in the background, crawling the graph and
 * performing arbitrary offline tasks.
 */
public class CrawlerModuleBootstrapper implements GraphAwareRuntimeModuleBootstrapper{

	@Override
	public GraphAwareRuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
		return new CrawlerRuntimeModule(moduleId);
	}

}
