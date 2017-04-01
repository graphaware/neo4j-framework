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

package com.graphaware.runtime.policy.all;

import com.graphaware.common.policy.inclusion.BasePropertyContainerInclusionPolicy;
import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 * Policy that includes all business / application level nodes, but exclude any
 * {@link com.graphaware.runtime.GraphAwareRuntime} internal nodes. Singleton.
 */
public final class IncludeAllBusinessNodes extends BasePropertyContainerInclusionPolicy<Node> implements NodeInclusionPolicy {

    static {
        Serializer.register(IncludeAllBusinessNodes.class, new SingletonSerializer());
    }

    private static final NodeInclusionPolicy INSTANCE = new IncludeAllBusinessNodes();

    public static NodeInclusionPolicy getInstance() {
        return INSTANCE;
    }

    private IncludeAllBusinessNodes() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node node) {
        for (Label label : node.getLabels()) {
            if (label.name().startsWith(RuntimeConfiguration.GA_PREFIX)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Node> doGetAll(GraphDatabaseService database) {
        return database.getAllNodes();
    }
}
