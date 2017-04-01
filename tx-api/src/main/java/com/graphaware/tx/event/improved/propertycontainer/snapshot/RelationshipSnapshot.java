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

import com.graphaware.common.wrapper.RelationshipWrapper;
import com.graphaware.tx.event.improved.data.PropertyContainerTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

/**
 * A {@link PropertyContainerSnapshot} representing a {@link org.neo4j.graphdb.Relationship}.
 */
public class RelationshipSnapshot extends PropertyContainerSnapshot<Relationship> implements Relationship, RelationshipWrapper {
    private static final Log LOG = LoggerFactory.getLogger(RelationshipSnapshot.class);

    /**
     * Construct a snapshot.
     *
     * @param wrapped                  relationship.
     * @param transactionDataContainer transaction data container.
     */
    public RelationshipSnapshot(Relationship wrapped, TransactionDataContainer transactionDataContainer) {
        super(wrapped, transactionDataContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyContainerTransactionData<Relationship> transactionData() {
        return transactionDataContainer.getRelationshipTransactionData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship self() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        if (transactionDataContainer.getRelationshipTransactionData().hasBeenDeleted(wrapped)) {
            LOG.warn("Relationship " + getId() + " has already been deleted in this transaction.");
        } else {
            super.delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node wrapNode(Node node) {
        return new NodeSnapshot(node, transactionDataContainer);
    }
}
