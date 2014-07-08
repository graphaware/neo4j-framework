package com.graphaware.example.pagerank;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.graphaware.common.strategy.IncludeAllRelationships;
import com.graphaware.common.strategy.InclusionStrategy;

/**
 * Simple implementation of {@link RelationshipChooser} that chooses at random one of the appropriate relationships using an
 * O(n) algorithm.
 */
public class RandomRelationshipChooser implements RelationshipChooser {

	private final InclusionStrategy<? super Relationship> inclusionStrategy;

	/**
	 * Constructs a new {@link RandomRelationshipChooser} that chooses any type of relationship.
	 */
	public RandomRelationshipChooser() {
		this(IncludeAllRelationships.getInstance());
	}

	/**
	 * Constructs a new {@link RandomRelationshipChooser} that chooses relationships in accordance with the given
	 * {@link InclusionStrategy}.
	 *
	 * @param inclusionStrategy The {@link InclusionStrategy} used to select relationships to follow
	 */
	public RandomRelationshipChooser(InclusionStrategy<? super Relationship> inclusionStrategy) {
		this.inclusionStrategy = inclusionStrategy;
	}

	@Override
	public Relationship chooseRelationship(Node node) {
		double max = 0.0;
		Relationship edgeChoice = null;
		// XXX: This will probably perform pretty poorly on popular nodes - is there an O(1) solution available?
        //todo: I'd use Reservoir sampler again: it's still O(n) but fewer LOC and tested. There is no O(1) solution AFAIK
		for (Relationship relationship : node.getRelationships()) {
			double rnd = Math.random();
			if (rnd > max) {
				max = rnd;
				edgeChoice = relationship;
			}
		}

		return edgeChoice;
	}

}
