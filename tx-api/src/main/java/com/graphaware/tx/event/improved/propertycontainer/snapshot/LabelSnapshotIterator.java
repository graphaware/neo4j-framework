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

package com.graphaware.tx.event.improved.propertycontainer.snapshot;

import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.helpers.collection.PrefetchingIterator;

import java.util.Iterator;

/**
 * {@link com.graphaware.tx.event.improved.propertycontainer.snapshot.NodeSnapshot}'s {@link org.neo4j.graphdb.Label} iterator.
 */
public class LabelSnapshotIterator extends PrefetchingIterator<Label> implements Iterator<Label>, Iterable<Label> {

    private final Node node;
    private final Iterator<Label> wrappedIterator;
    private final TransactionDataContainer transactionDataContainer;
    private final Iterator<Label> removedLabelsIterator;

    public LabelSnapshotIterator(Node node, Iterable<Label> wrappedIterable, TransactionDataContainer transactionDataContainer) {
        this.node = node;
        this.wrappedIterator = wrappedIterable.iterator();
        this.transactionDataContainer = transactionDataContainer;
        if (transactionDataContainer.getNodeTransactionData().hasBeenChanged(node)) {
            this.removedLabelsIterator = transactionDataContainer.getNodeTransactionData().removedLabels(node).iterator();
        }
        else if (transactionDataContainer.getNodeTransactionData().hasBeenDeleted(node)) {
            this.removedLabelsIterator = transactionDataContainer.getNodeTransactionData().labelsOfDeletedNode(node).iterator();
        }
        else {
            this.removedLabelsIterator = Iterators.emptyIterator();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Label> iterator() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Label fetchNextOrNull() {
        while (wrappedIterator.hasNext()) {
            Label next = wrappedIterator.next();
            if (transactionDataContainer.getNodeTransactionData().hasLabelBeenAssigned(node, next)) {
                //just assigned - wasn't there before the TX started
                continue;
            }

            return next;
        }

        if (removedLabelsIterator.hasNext()) {
            return removedLabelsIterator.next();
        }

        return null;
    }
}
