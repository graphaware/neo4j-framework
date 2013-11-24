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

package com.graphaware.tx.event.improved.data.filtered;

import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.data.PropertyContainerTransactionData;
import com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredNode;
import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.PropertyContainerInclusionStrategy;
import com.graphaware.common.strategy.PropertyInclusionStrategy;
import org.neo4j.graphdb.Node;

/**
 * {@link FilteredPropertyContainerTransactionData} for {@link org.neo4j.graphdb.Node}s.
 */
public class FilteredNodeTransactionData extends FilteredPropertyContainerTransactionData<Node> implements NodeTransactionData {

    private final NodeTransactionData wrapped;

    /**
     * Construct filtered node transaction data.
     *
     * @param wrapped    wrapped node transaction data.
     * @param strategies for filtering.
     */
    public FilteredNodeTransactionData(NodeTransactionData wrapped, InclusionStrategies strategies) {
        super(strategies);
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyContainerTransactionData<Node> getWrapped() {
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node filtered(Node original) {
        return new FilteredNode(original, strategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyContainerInclusionStrategy<Node> getPropertyContainerInclusionStrategy() {
        return strategies.getNodeInclusionStrategy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyInclusionStrategy<Node> getPropertyInclusionStrategy() {
        return strategies.getNodePropertyInclusionStrategy();
    }
}
