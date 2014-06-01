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

package com.graphaware.tx.event.improved.data.lazy;

import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import com.graphaware.tx.event.improved.propertycontainer.snapshot.NodeSnapshot;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;

import java.util.HashMap;

import static com.graphaware.common.util.PropertyContainerUtils.id;

/**
 * {@link LazyPropertyContainerTransactionData} for {@link org.neo4j.graphdb.Node}s.
 */
public class LazyNodeTransactionData extends LazyPropertyContainerTransactionData<Node> implements NodeTransactionData {

    private final TransactionData transactionData;
    private final TransactionDataContainer transactionDataContainer;

    /**
     * Construct node transaction data from Neo4j {@link org.neo4j.graphdb.event.TransactionData}.
     *
     * @param transactionData          provided by Neo4j.
     * @param transactionDataContainer containing {@link com.graphaware.tx.event.improved.data.PropertyContainerTransactionData}..
     */
    public LazyNodeTransactionData(TransactionData transactionData, TransactionDataContainer transactionDataContainer) {
        this.transactionData = transactionData;
        this.transactionDataContainer = transactionDataContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node oldSnapshot(Node original) {
        return new NodeSnapshot(original, transactionDataContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node newSnapshot(Node original) {
        return original;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Node> created() {
        return transactionData.createdNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Node> deleted() {
        return transactionData.deletedNodes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<PropertyEntry<Node>> assignedProperties() {
        return transactionData.assignedNodeProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<PropertyEntry<Node>> removedProperties() {
        return transactionData.removedNodeProperties();
    }

    protected void initializeChanged() {
        super.initializeChanged();

        for (transactionData.)

        for (PropertyEntry<T> propertyEntry : assignedProperties()) {
            if (hasNotActuallyChanged(propertyEntry)) {
                continue;
            }

            T candidate = propertyEntry.entity();
            if (!hasBeenCreated(candidate) && !changed.containsKey(id(candidate))) {
                Change<T> change = new Change<>(oldSnapshot(candidate), newSnapshot(candidate));
                changed.put(id(candidate), change);
            }
        }

    }
}
