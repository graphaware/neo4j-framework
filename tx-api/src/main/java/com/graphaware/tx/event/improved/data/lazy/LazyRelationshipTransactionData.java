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

import com.graphaware.tx.event.improved.data.RelationshipTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import com.graphaware.tx.event.improved.propertycontainer.snapshot.RelationshipSnapshot;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.graphaware.common.util.DirectionUtils.matches;
import static org.neo4j.graphdb.Direction.BOTH;

/**
 * {@link LazyPropertyContainerTransactionData} for {@link org.neo4j.graphdb.Relationship}s.
 */
public class LazyRelationshipTransactionData extends LazyPropertyContainerTransactionData<Relationship> implements RelationshipTransactionData {

    private final TransactionData transactionData;
    private final TransactionDataContainer transactionDataContainer;

    /**
     * Construct relationship transaction data from Neo4j {@link org.neo4j.graphdb.event.TransactionData}.
     *
     * @param transactionData          provided by Neo4j.
     * @param transactionDataContainer containing {@link com.graphaware.tx.event.improved.data.PropertyContainerTransactionData}.
     */
    public LazyRelationshipTransactionData(TransactionData transactionData, TransactionDataContainer transactionDataContainer) {
        this.transactionData = transactionData;
        this.transactionDataContainer = transactionDataContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship oldSnapshot(Relationship original) {
        return new RelationshipSnapshot(original, transactionDataContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship newSnapshot(Relationship original) {
        return original;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Relationship> created() {
        return transactionData.createdRelationships();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Relationship> deleted() {
        return transactionData.deletedRelationships();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<PropertyEntry<Relationship>> assignedProperties() {
        return transactionData.assignedRelationshipProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<PropertyEntry<Relationship>> removedProperties() {
        return transactionData.removedRelationshipProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getDeleted(Node node, RelationshipType... types) {
        return getDeleted(node, BOTH, types);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getDeleted(Node node, Direction direction, RelationshipType... types) {
        Set<String> typeNames = new HashSet<>();
        for (RelationshipType type : types) {
            typeNames.add(type.name());
        }

        Set<Relationship> result = new HashSet<>();
        for (Relationship deleted : getAllDeleted()) {
            if ((typeNames.isEmpty() || typeNames.contains(deleted.getType().name())) &&
                    ((deleted.getStartNode().getId() == node.getId() && matches(deleted, deleted.getStartNode(), direction))
                            || (deleted.getEndNode().getId() == node.getId() && matches(deleted, deleted.getEndNode(), direction)))) {
                result.add(deleted);
            }
        }

        return result;
    }
}
