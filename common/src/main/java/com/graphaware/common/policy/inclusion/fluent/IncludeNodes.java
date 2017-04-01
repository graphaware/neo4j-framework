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

package com.graphaware.common.policy.inclusion.fluent;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.DetachedPropertiesDescription;
import com.graphaware.common.description.property.WildcardPropertiesDescription;
import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Collections;

/**
 * An implementation of {@link NodeInclusionPolicy} that is entirely configurable using its fluent interface.
 */
public class IncludeNodes extends BaseIncludeNodes<IncludeNodes> {

    /**
     * Get a node inclusion policy that includes all nodes as the base-line for further configuration.
     * <p/>
     * Note that if you want to simply include all nodes, it is more efficient to use {@link com.graphaware.common.policy.inclusion.all.IncludeAllNodes}.
     *
     * @return a policy including all nodes.
     */
    public static IncludeNodes all() {
        return new IncludeNodes(null, new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Create a new policy.
     *
     * @param label                 that matching nodes must have, can be null for all labels.
     * @param propertiesDescription of the matching nodes.
     */
    protected IncludeNodes(String label, DetachedPropertiesDescription propertiesDescription) {
        super(label, propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeNodes newInstance(String label) {
        return new IncludeNodes(label, getPropertiesDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IncludeNodes newInstance(DetachedPropertiesDescription propertiesDescription) {
        return new IncludeNodes(getLabel(), propertiesDescription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Node> doGetAll(GraphDatabaseService database) {
        return database.getAllNodes();
    }
}
