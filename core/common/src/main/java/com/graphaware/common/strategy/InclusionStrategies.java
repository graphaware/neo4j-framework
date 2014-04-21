/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.common.strategy;

/**
 * Base-class for {@link InclusionStrategies} implementations.
 */
public class InclusionStrategies {

    private final NodeInclusionStrategy nodeInclusionStrategy;
    private final NodePropertyInclusionStrategy nodePropertyInclusionStrategy;
    private final RelationshipInclusionStrategy relationshipInclusionStrategy;
    private final RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy;

    /**
     * Create all-including strategies.
     *
     * @return all-including strategies.
     */
    public static InclusionStrategies all() {
        return new InclusionStrategies(
                IncludeAllNodes.getInstance(),
                IncludeAllNodeProperties.getInstance(),
                IncludeAllRelationships.getInstance(),
                IncludeAllRelationshipProperties.getInstance());
    }

    /**
     * Create nothing-including strategies.
     *
     * @return nothing-including strategies.
     */
    public static InclusionStrategies none() {
        return new InclusionStrategies(
                IncludeNoNodes.getInstance(),
                IncludeNoNodeProperties.getInstance(),
                IncludeNoRelationships.getInstance(),
                IncludeNoRelationshipProperties.getInstance());
    }

    /**
     * Constructor.
     *
     * @param nodeInclusionStrategy         strategy.
     * @param nodePropertyInclusionStrategy strategy.
     * @param relationshipInclusionStrategy strategy.
     * @param relationshipPropertyInclusionStrategy
     *                                      strategy.
     */
    public InclusionStrategies(NodeInclusionStrategy nodeInclusionStrategy, NodePropertyInclusionStrategy nodePropertyInclusionStrategy, RelationshipInclusionStrategy relationshipInclusionStrategy, RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        this.nodeInclusionStrategy = nodeInclusionStrategy;
        this.nodePropertyInclusionStrategy = nodePropertyInclusionStrategy;
        this.relationshipInclusionStrategy = relationshipInclusionStrategy;
        this.relationshipPropertyInclusionStrategy = relationshipPropertyInclusionStrategy;
    }

    /**
     * Reconfigure this instance to use a custom node inclusion strategy.
     *
     * @param nodeInclusionStrategy to use.
     * @return reconfigured strategies.
     */
    public InclusionStrategies with(NodeInclusionStrategy nodeInclusionStrategy) {
        return new InclusionStrategies(nodeInclusionStrategy, getNodePropertyInclusionStrategy(), getRelationshipInclusionStrategy(), getRelationshipPropertyInclusionStrategy());
    }

    /**
     * Reconfigure this instance to use a custom node property inclusion strategy.
     *
     * @param nodePropertyInclusionStrategy to use.
     * @return reconfigured strategies.
     */
    public InclusionStrategies with(NodePropertyInclusionStrategy nodePropertyInclusionStrategy) {
        return new InclusionStrategies(getNodeInclusionStrategy(), nodePropertyInclusionStrategy, getRelationshipInclusionStrategy(), getRelationshipPropertyInclusionStrategy());
    }

    /**
     * Reconfigure this instance to use a custom relationship inclusion strategy.
     *
     * @param relationshipInclusionStrategy to use.
     * @return reconfigured strategies.
     */
    public InclusionStrategies with(RelationshipInclusionStrategy relationshipInclusionStrategy) {
        return new InclusionStrategies(getNodeInclusionStrategy(), getNodePropertyInclusionStrategy(), relationshipInclusionStrategy, getRelationshipPropertyInclusionStrategy());
    }

    /**
     * Reconfigure this instance to use a custom relationship property inclusion strategy.
     *
     * @param relationshipPropertyInclusionStrategy
     *         to use.
     * @return reconfigured strategies.
     */
    public InclusionStrategies with(RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        return new InclusionStrategies(getNodeInclusionStrategy(), getNodePropertyInclusionStrategy(), getRelationshipInclusionStrategy(), relationshipPropertyInclusionStrategy);
    }

    /**
     * @return contained node inclusion strategy.
     */
    public NodeInclusionStrategy getNodeInclusionStrategy() {
        return nodeInclusionStrategy;
    }

    /**
     * @return contained node property inclusion strategy.
     */
    public NodePropertyInclusionStrategy getNodePropertyInclusionStrategy() {
        return nodePropertyInclusionStrategy;
    }

    /**
     * @return contained relationship inclusion strategy.
     */
    public RelationshipInclusionStrategy getRelationshipInclusionStrategy() {
        return relationshipInclusionStrategy;
    }

    /**
     * @return contained relationship property inclusion strategy.
     */
    public RelationshipPropertyInclusionStrategy getRelationshipPropertyInclusionStrategy() {
        return relationshipPropertyInclusionStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InclusionStrategies that = (InclusionStrategies) o;

        if (!nodeInclusionStrategy.equals(that.nodeInclusionStrategy)) return false;
        if (!nodePropertyInclusionStrategy.equals(that.nodePropertyInclusionStrategy)) return false;
        if (!relationshipInclusionStrategy.equals(that.relationshipInclusionStrategy)) return false;
        if (!relationshipPropertyInclusionStrategy.equals(that.relationshipPropertyInclusionStrategy)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nodeInclusionStrategy.hashCode();
        result = 31 * result + nodePropertyInclusionStrategy.hashCode();
        result = 31 * result + relationshipInclusionStrategy.hashCode();
        result = 31 * result + relationshipPropertyInclusionStrategy.hashCode();
        return result;
    }
}
