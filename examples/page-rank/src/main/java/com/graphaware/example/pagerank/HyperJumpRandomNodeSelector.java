package com.graphaware.example.pagerank;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.common.util.ReservoirSampler;

/**
 * Very simple implementation of {@link RandomNodeSelector} that chooses an arbitrary node from the graph database in O(n) time.
 */
public class HyperJumpRandomNodeSelector implements RandomNodeSelector {

	private final InclusionStrategy<? super Node> inclusionStrategy;

	/**
	 * Constructs a new {@link HyperJumpRandomNodeSelector} that selects any node.
	 */
	public HyperJumpRandomNodeSelector() {
		this(IncludeAllNodes.getInstance());
	}

	/**
	 * Constructs a new {@link HyperJumpRandomNodeSelector} that selects only nodes matched by the given
	 * {@link InclusionStrategy}.
	 *
	 * @param inclusionStrategy The {@link InclusionStrategy} to consider when selecting nodes.
	 */
	public HyperJumpRandomNodeSelector(InclusionStrategy<? super Node> inclusionStrategy) {
		this.inclusionStrategy = inclusionStrategy;
	}

    //todo: we should really make this O(1) by finding the highest node id and choosing a random number between 0 and the highest, retrying if the node does not exist
	// we should, aye, but first we need to figure out
	@Override
	public Node selectRandomNode(GraphDatabaseService database) {
		Iterable<Node> allNodes = GlobalGraphOperations.at(database).getAllNodes();

		ReservoirSampler<Node> randomSampler = new ReservoirSampler<>(1);
		for (Node node : allNodes) {
			if (this.inclusionStrategy.include(node)) {
				randomSampler.sample(node);
			}
		}
		return randomSampler.getSamples().iterator().next();
	}

}
