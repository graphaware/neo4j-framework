/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.walk;

import com.graphaware.common.strategy.NodeInclusionStrategy;
import com.graphaware.common.util.ReservoirSampler;
import com.graphaware.runtime.strategy.IncludeAllBusinessNodes;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * {@link NodeSelector} that selects a {@link Node} at random from all {@link Node}s available in the database that match
 * the provided {@link com.graphaware.common.strategy.NodeInclusionStrategy}.
 */
public class RandomNodeSelector implements NodeSelector {

    private static final int MAX_EFFICIENT_ATTEMPTS = 10;

    private final NodeInclusionStrategy inclusionStrategy;
    private final RandomDataGenerator random = new RandomDataGenerator();

    /**
     * Constructs a new {@link RandomNodeSelector} that selects any node which isn't a framework-internal node.
     */
    public RandomNodeSelector() {
        this(IncludeAllBusinessNodes.getInstance());
    }

    /**
     * Constructs a new {@link RandomNodeSelector} that selects a random node that matches the given
     * {@link com.graphaware.common.strategy.InclusionStrategy}.
     *
     * @param inclusionStrategy The {@link com.graphaware.common.strategy.InclusionStrategy} to consider when selecting
     *                          nodes.
     */
    public RandomNodeSelector(NodeInclusionStrategy inclusionStrategy) {
        this.inclusionStrategy = inclusionStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node selectNode(GraphDatabaseService database) {
        Node candidate = randomNodeO1(database);

        if (candidate != null) {
            return candidate;
        }

        return randomNodeON(database);
    }

    /**
     * Get a random node in O(1), try only 10 attempts.
     *
     * @param database in which to find a random node.
     * @return random node, null if not successful.
     */
    private Node randomNodeO1(GraphDatabaseService database) {
        long highestId = ((GraphDatabaseAPI) database).getDependencyResolver().resolveDependency(NodeManager.class).getHighestPossibleIdInUse(Node.class);

        if (highestId <= 0) {
            return null;
        }

        for (int i = 0; i < MAX_EFFICIENT_ATTEMPTS; i++) {
            long randomId = random.nextLong(0, highestId);
            try {
                Node node = database.getNodeById(randomId);
                if (inclusionStrategy.include(node)) {
                    return node;
                }
            } catch (NotFoundException e) {
                //ok, try again
            }
        }

        return null;
    }

    /**
     * Get a random node in O(N).
     *
     * @param database in which to find a random node.
     * @return random node, null if not successful.
     */
    private Node randomNodeON(GraphDatabaseService database) {
        Iterable<Node> allNodes = GlobalGraphOperations.at(database).getAllNodes();

        ReservoirSampler<Node> randomSampler = new ReservoirSampler<>(1);
        for (Node node : allNodes) {
            if (this.inclusionStrategy.include(node)) {
                randomSampler.sample(node);
            }
        }

        if (randomSampler.isEmpty()) {
            return null;
        }

        return randomSampler.getSamples().iterator().next();
    }
}
