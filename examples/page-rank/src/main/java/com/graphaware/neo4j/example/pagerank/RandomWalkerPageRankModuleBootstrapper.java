package com.graphaware.neo4j.example.pagerank;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphaware.runtime.module.RuntimeModuleBootstrapper;

/**
 * {@link RuntimeModuleBootstrapper} used by the GraphAware runtime to prepare the {@link RandomWalkerPageRankModule}.
 */
public class RandomWalkerPageRankModuleBootstrapper implements RuntimeModuleBootstrapper {

	private static final Logger LOG = LoggerFactory.getLogger(RandomWalkerPageRankModuleBootstrapper.class);

	@Override
	public RandomWalkerPageRankModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
		LOG.info("Constructing new module with ID: {}", moduleId);
System.out.println("Config is: " + config);
		return new RandomWalkerPageRankModule(moduleId);
	}

}
