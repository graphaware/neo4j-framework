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

package com.graphaware.tx.event.improved.api;

import com.graphaware.common.strategy.*;
import com.graphaware.tx.event.improved.data.BaseImprovedTransactionData;
import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.data.RelationshipTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import com.graphaware.tx.event.improved.data.filtered.FilteredNodeTransactionData;
import com.graphaware.tx.event.improved.data.filtered.FilteredRelationshipTransactionData;

/**
 * {@link ImprovedTransactionData} with filtering capabilities defined by {@link com.graphaware.common.strategy.InclusionStrategies}, delegating to
 * {@link com.graphaware.tx.event.improved.data.filtered.FilteredNodeTransactionData} and {@link com.graphaware.tx.event.improved.data.filtered.FilteredRelationshipTransactionData}.
 * <p/>
 * Results of methods returning {@link java.util.Collection}s and {@link java.util.Map}s will be filtered. <code>boolean</code>
 * and single object returning methods will always return the full truth no matter the strategies. So for example:
 * <p/>
 * {@link #getAllCreatedNodes()} can return 5 nodes, but {@link #hasBeenCreated(org.neo4j.graphdb.Node)}  can
 * return true for more of them, as it ignores the filtering.
 * <p/>
 * When traversing the graph using an object returned by this API (such as {@link com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredNode}),
 * nodes, properties, and relationships not included by the {@link com.graphaware.common.strategy.InclusionStrategies} will be excluded. The only exception
 * to this are relationship start and end nodes - they are returned even if they would normally be filtered out. This is
 * a design decision in order to honor the requirement that relationships must have start and end node.
 */
public class FilteredTransactionData extends BaseImprovedTransactionData implements ImprovedTransactionData, TransactionDataContainer {

    private final InclusionStrategies inclusionStrategies;
    private final NodeTransactionData nodeTransactionData;
    private final RelationshipTransactionData relationshipTransactionData;

    /**
     * Construct a new filtered transaction data.
     *
     * @param transactionDataContainer container for original unfiltered transaction data.
     * @param inclusionStrategies      strategies for filtering.
     */
    public FilteredTransactionData(TransactionDataContainer transactionDataContainer, InclusionStrategies inclusionStrategies) {
        this.inclusionStrategies = inclusionStrategies;
        nodeTransactionData = new FilteredNodeTransactionData(transactionDataContainer.getNodeTransactionData(), inclusionStrategies);
        relationshipTransactionData = new FilteredRelationshipTransactionData(transactionDataContainer.getRelationshipTransactionData(), inclusionStrategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeTransactionData getNodeTransactionData() {
        return nodeTransactionData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipTransactionData getRelationshipTransactionData() {
        return relationshipTransactionData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mutationsOccurred() {
        //overridden for optimization - we don't want to load things (and especially properties) if we don't need to
        return (!inclusionStrategies.getNodeInclusionStrategy().equals(IncludeNoNodes.getInstance()) && !getAllCreatedNodes().isEmpty())
                || (!inclusionStrategies.getRelationshipInclusionStrategy().equals(IncludeNoRelationships.getInstance()) && !getAllCreatedRelationships().isEmpty())
                || (!inclusionStrategies.getNodeInclusionStrategy().equals(IncludeNoNodes.getInstance()) && !getAllDeletedNodes().isEmpty())
                || (!inclusionStrategies.getRelationshipInclusionStrategy().equals(IncludeNoRelationships.getInstance()) && !getAllDeletedRelationships().isEmpty())
                || (!inclusionStrategies.getNodePropertyInclusionStrategy().equals(IncludeNoNodeProperties.getInstance()) && !getAllChangedNodes().isEmpty())
                || (!inclusionStrategies.getRelationshipPropertyInclusionStrategy().equals(IncludeNoRelationshipProperties.getInstance()) && !getAllChangedRelationships().isEmpty());
    }
}
