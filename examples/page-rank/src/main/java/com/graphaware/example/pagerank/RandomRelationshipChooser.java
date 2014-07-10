package com.graphaware.example.pagerank;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.common.util.ReservoirSampler;

/**
 * Simple implementation of {@link RelationshipChooser} that chooses at random one of the appropriate relationships using an
 * O(n) algorithm.
 */
public class RandomRelationshipChooser implements RelationshipChooser {

	private final InclusionStrategy<? super Relationship> relationshipInclusionStrategy;
	private final InclusionStrategy<? super Node> nodeInclusionStrategy;

	/**
	 * Constructs a new {@link RandomRelationshipChooser} that chooses any type of relationship.
	 */
	public RandomRelationshipChooser() {
		this(IncludeAllRelationships.getInstance(), IncludeAllNodes.getInstance());
	}

	/**
	 * Constructs a new {@link RandomRelationshipChooser} that chooses relationships in accordance with the given
	 * {@link InclusionStrategy}.
	 *
	 * @param relationshipInclusionStrategy The {@link InclusionStrategy} used to select relationships to follow
	 * @param nodeInclusionStrategy The {@link InclusionStrategy} used to select relationships that point only to certain nodes
	 */
	public RandomRelationshipChooser(InclusionStrategy<? super Relationship> relationshipInclusionStrategy,
			InclusionStrategy<? super Node> nodeInclusionStrategy) {
		this.relationshipInclusionStrategy = relationshipInclusionStrategy;
		this.nodeInclusionStrategy = nodeInclusionStrategy;
	}

	@Override
	public Relationship chooseRelationship(Node node) {
		// XXX: This will probably perform pretty poorly on popular nodes, but there's no known O(1) solution
		ReservoirSampler<Relationship> randomSampler = new ReservoirSampler<>(1);
		for (Relationship relationship : node.getRelationships()) {
			if (this.relationshipInclusionStrategy.include(relationship)
					&& this.nodeInclusionStrategy.include(relationship.getOtherNode(node))) {
				randomSampler.sample(relationship);
			}
		}

		return randomSampler.getSamples().iterator().next();
	}

}
