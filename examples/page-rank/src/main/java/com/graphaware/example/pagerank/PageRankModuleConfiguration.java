package com.graphaware.example.pagerank;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.InclusionStrategy;

/**
 * Contains configuration settings for the page rank module.
 */
public class PageRankModuleConfiguration {

	private final InclusionStrategy<? super Node> nodeInclusionStrategy;
	private final InclusionStrategy<? super Relationship> relationshipInclusionStrategy;

	/**
	 * Retrieves the default {@link PageRankModuleConfiguration}, which includes all nodes and relationships.
	 *
	 * @return The default {@link PageRankModuleConfiguration}
	 */
	public static PageRankModuleConfiguration defaultConfiguration() {
		return new PageRankModuleConfiguration(IncludeAllNodes.getInstance(), IncludeAllRelationships.getInstance());
	}

	/**
	 * Constructs a new {@link PageRankModuleConfiguration} based on the given inclusion strategies.
	 *
	 * @param nodeInclusionStrategy The {@link InclusionStrategy} to use for selecting nodes to include in the page rank
	 *        algorithm
	 * @param relationshipInclusionStrategy The {@link InclusionStrategy} for selecting which relationships to follow when
	 *        crawling the graph
	 */
	public PageRankModuleConfiguration(InclusionStrategy<? super Node> nodeInclusionStrategy,
			InclusionStrategy<? super Relationship> relationshipInclusionStrategy) {

		this.nodeInclusionStrategy = nodeInclusionStrategy;
		this.relationshipInclusionStrategy = relationshipInclusionStrategy;
	}

	public InclusionStrategy<? super Node> getNodeInclusionStrategy() {
		return nodeInclusionStrategy;
	}

	public InclusionStrategy<? super Relationship> getRelationshipInclusionStrategy() {
		return relationshipInclusionStrategy;
	}

}
