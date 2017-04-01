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

package com.graphaware.common.policy.inclusion.spel;

import com.graphaware.common.policy.inclusion.NodePropertyInclusionPolicy;
import com.graphaware.common.representation.AttachedNode;
import com.graphaware.common.representation.AttachedNodeProperty;
import com.graphaware.common.representation.NodeProperty;
import org.neo4j.graphdb.Node;

/**
 * {@link NodePropertyInclusionPolicy} based on a SPEL expression. The expression can use methods defined in
 * {@link NodeProperty}.
 */
public class SpelNodePropertyInclusionPolicy extends SpelInclusionPolicy implements NodePropertyInclusionPolicy {

    public SpelNodePropertyInclusionPolicy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, Node node) {
        return (Boolean) exp.getValue(new AttachedNodeProperty(key, new AttachedNode(node)));
    }
}
