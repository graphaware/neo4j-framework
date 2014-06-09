package com.graphaware.crawler.bootstrap;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.crawler.CrawlerRuntimeModule;
import com.graphaware.crawler.api.Context;
import com.graphaware.crawler.api.ThingThatGetsCalledWhenWeFindSomething;
import com.graphaware.runtime.GraphAwareRuntimeModule;
import com.graphaware.runtime.GraphAwareRuntimeModuleBootstrapper;
import com.graphaware.runtime.config.MinimalRuntimeModuleConfiguration;
import com.graphaware.runtime.config.RuntimeModuleConfiguration;

/**
 * {@link GraphAwareRuntimeModuleBootstrapper} for the module that beavers away in the background, crawling the graph and
 * performing arbitrary offline tasks.
 */
public class CrawlerModuleBootstrapper implements GraphAwareRuntimeModuleBootstrapper {

	@Override
	public GraphAwareRuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
		RuntimeModuleConfiguration moduleConfiguration = new MinimalRuntimeModuleConfiguration()
				.with(IncludeAllNodes.getInstance())
				.with(IncludeAllRelationships.getInstance());

		return new CrawlerRuntimeModule(moduleId, moduleConfiguration, new ThingThatGetsCalledWhenWeFindSomething() {
			@Override
			public void doSomeStuff(Context context) {
				throw new UnsupportedOperationException("atg hasn't written this method yet");
			}
		});
	}

}
