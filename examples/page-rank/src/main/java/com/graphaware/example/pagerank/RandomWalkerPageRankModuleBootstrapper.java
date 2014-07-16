package com.graphaware.example.pagerank;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.example.pagerank.parser.ModuleConfigParameterParser;
import com.graphaware.example.pagerank.parser.RegexModuleConfigParameterParser;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;

/**
 * {@link RuntimeModuleBootstrapper} used by the {@link com.graphaware.runtime.GraphAwareRuntime} to prepare the
 * {@link RandomWalkerPageRankModule}.
 */
public class RandomWalkerPageRankModuleBootstrapper implements RuntimeModuleBootstrapper {

	private static final Logger LOG = LoggerFactory.getLogger(RandomWalkerPageRankModuleBootstrapper.class);

	private final ModuleConfigParameterParser configParameterParser = new RegexModuleConfigParameterParser();

	@Override
	public RandomWalkerPageRankModule bootstrapModule(String moduleId, Map<String, String> configParams, GraphDatabaseService database) {
		LOG.info("Constructing new module with ID: {}", moduleId);
		LOG.trace("Configuration parameter map is: {}", configParams);

		// parse Cypher-like expressions to configure inclusion strategies
		InclusionStrategy<Node> nodeInclusionStrategy = configParams.containsKey("inclusionStrategy.node")
				? this.configParameterParser.parseForNodeInclusionStrategy(configParams.get("inclusionStrategy.node"))
				: IncludeAllNodes.getInstance();
		InclusionStrategy<Relationship> relationshipInclusionStrategy = configParams.containsKey("inclusionStrategy.relationship")
				? this.configParameterParser.parseForRelationshipInclusionStrategy(configParams.get("inclusionStrategy.relationship"))
				: IncludeAllRelationships.getInstance();

		return new RandomWalkerPageRankModule(moduleId,
				new PageRankModuleConfiguration(nodeInclusionStrategy, relationshipInclusionStrategy));
	}

}
