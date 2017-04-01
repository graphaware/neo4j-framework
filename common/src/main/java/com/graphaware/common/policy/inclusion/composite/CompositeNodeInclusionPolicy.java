/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.policy.inclusion.composite;

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * {@link CompositePropertyContainerInclusionPolicy} for {@link Node}s.
 */
public final class CompositeNodeInclusionPolicy extends CompositePropertyContainerInclusionPolicy<Node, NodeInclusionPolicy> implements NodeInclusionPolicy {

    public static CompositeNodeInclusionPolicy of(NodeInclusionPolicy... policies) {
        return new CompositeNodeInclusionPolicy(policies);
    }

    private CompositeNodeInclusionPolicy(NodeInclusionPolicy[] policies) {
        super(policies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Node> doGetAll(GraphDatabaseService database) {
        return database.getAllNodes();
    }
}
