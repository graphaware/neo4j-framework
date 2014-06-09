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

/**
 * {@link GraphAwareRuntimeModuleBootstrapper} for the module that beavers away in the background, crawling the graph and
 * performing arbitrary offline tasks.
 */
public class CrawlerModuleBootstrapper implements GraphAwareRuntimeModuleBootstrapper {

	@Override
	public GraphAwareRuntimeModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
		return new CrawlerRuntimeModule(moduleId, 
                                                IncludeAllNodes.getInstance(),  
                                                IncludeAllRelationships.getInstance(), 
                                                new ThingThatGetsCalledWhenWeFindSomething() {
			@Override
			public void doSomeStuff(Context context) {
				throw new UnsupportedOperationException("atg hasn't written this method yet");
			}
		});
	}

}
