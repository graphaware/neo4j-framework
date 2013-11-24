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
 * {@link InclusionStrategies}, providing static factory method for default
 * "all"/"none" configurations and "with" methods for fluently overriding these with custom strategies.
 */
public class InclusionStrategiesImpl extends BaseInclusionStrategies<InclusionStrategiesImpl> implements InclusionStrategies {

    /**
     * Create all-including strategies.
     *
     * @return all-including strategies.
     */
    public static InclusionStrategiesImpl all() {
        return new InclusionStrategiesImpl(
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
    public static InclusionStrategiesImpl none() {
        return new InclusionStrategiesImpl(
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
    private InclusionStrategiesImpl(NodeInclusionStrategy nodeInclusionStrategy, NodePropertyInclusionStrategy nodePropertyInclusionStrategy, RelationshipInclusionStrategy relationshipInclusionStrategy, RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        super(nodeInclusionStrategy, nodePropertyInclusionStrategy, relationshipInclusionStrategy, relationshipPropertyInclusionStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InclusionStrategiesImpl newInstance(NodeInclusionStrategy nodeInclusionStrategy, NodePropertyInclusionStrategy nodePropertyInclusionStrategy, RelationshipInclusionStrategy relationshipInclusionStrategy, RelationshipPropertyInclusionStrategy relationshipPropertyInclusionStrategy) {
        return new InclusionStrategiesImpl(nodeInclusionStrategy, nodePropertyInclusionStrategy, relationshipInclusionStrategy, relationshipPropertyInclusionStrategy);
    }
}
