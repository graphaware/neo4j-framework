package com.graphaware.crawler;

import com.graphaware.common.strategy.IncludeNoNodeProperties;
import com.graphaware.common.strategy.IncludeNoRelationshipProperties;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;
import com.graphaware.crawler.internal.PerpetualGraphCrawler;

/**
 * Implementation of {@link CrawlerModuleConfiguration} constructed with custom arguments to suit particular usage requirements.
 */
public class CustomCrawlerModuleConfiguration implements CrawlerModuleConfiguration {

	private final InclusionStrategies inclusionStrategies;
	private final PerpetualGraphCrawler perpetualGraphCrawler;

	/**
	 * Constructs a new {@link CustomCrawlerModuleConfiguration} based on the given arguments.  Passing any of the
	 * arguments as <code>null</code> has an undefined behaviour, but will most likely break things later on.
	 *
	 * @param nodeInclusionStrategy The {@link NodeInclusionStrategy} to use
	 * @param relationshipInclusionStrategy The {@link RelationshipInclusionStrategy} to use
	 * @param perpetualGraphCrawler The {@link PerpetualGraphCrawler} implementation to use
	 */
	public CustomCrawlerModuleConfiguration(NodeInclusionStrategy nodeInclusionStrategy,
			RelationshipInclusionStrategy relationshipInclusionStrategy, PerpetualGraphCrawler perpetualGraphCrawler) {

		this.perpetualGraphCrawler = perpetualGraphCrawler;
		this.inclusionStrategies = new InclusionStrategies(nodeInclusionStrategy, IncludeNoNodeProperties.getInstance(),
				relationshipInclusionStrategy, IncludeNoRelationshipProperties.getInstance());
	}

	@Override
	public InclusionStrategies getInclusionStrategies() {
		return this.inclusionStrategies;
	}

	@Override
	public PerpetualGraphCrawler getPerpetualGraphCrawler() {
		return this.perpetualGraphCrawler;
	}

}
