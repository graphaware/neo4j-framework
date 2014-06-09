package com.graphaware.crawler.bootstrap;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.crawler.CrawlerModuleConfiguration;
import com.graphaware.crawler.CrawlerRuntimeModule;
import com.graphaware.crawler.DefaultCrawlerModuleConfiguration;
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
		CrawlerModuleConfiguration moduleConfiguration = new DefaultCrawlerModuleConfiguration();

		// I thought about putting the ThingThatGetsCalled... into the configuration, but keeping it as a separate part of the
		// module's constructor signature makes its importance more obvious.  We can default anything in the configuration object
		// but we can't default this.
		return new CrawlerRuntimeModule(moduleId, moduleConfiguration, new ThingThatGetsCalledWhenWeFindSomething() {
			@Override
			public void doSomeStuff(Context context) {
				throw new UnsupportedOperationException("atg hasn't written this method yet");
			}
		});
	}

}
