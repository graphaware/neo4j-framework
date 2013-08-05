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

package com.graphaware.tx.event.improved.strategy;

import com.graphaware.framework.GraphAwareFramework;
import com.graphaware.framework.config.FrameworkConfiguration;
import org.neo4j.graphdb.Node;

/**
 * Base-class for all {@link NodeInclusionStrategy} implementations
 * that include arbitrary business / application level nodes (up to subclasses to decide which ones), but exclude any
 * {@link GraphAwareFramework}/{@link com.graphaware.framework.GraphAwareModule} internal nodes.
 */
public abstract class IncludeAllBusinessNodes implements NodeInclusionStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean include(Node node) {
        if (node.hasProperty(FrameworkConfiguration.GA_NODE_PROPERTY_KEY)) {
            return false;
        }

        return doInclude(node);
    }

    /**
     * Should this node be included?
     *
     * @param node to check.
     * @return true iff the node should be included.
     */
    protected abstract boolean doInclude(Node node);

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.getClass().getCanonicalName().hashCode();
    }
}
