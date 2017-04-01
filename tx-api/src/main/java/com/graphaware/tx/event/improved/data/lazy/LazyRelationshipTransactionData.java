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

package com.graphaware.tx.event.improved.data.lazy;

import com.graphaware.common.util.Change;
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
    public Collection<Relationship> getCreated(Node node, RelationshipType... types) {
        return getCreated(node, BOTH, types);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getCreated(Node node, Direction direction, RelationshipType... types) {
        return filterRelationships(getAllCreated(), node, direction, types);
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
        return filterRelationships(getAllDeleted(), node, direction, types);
    }

    /**
     * Filter relationships based on type and direction.
     *
     * @param node      whose point of view we're looking.
     * @param direction of the relationships to be incuded.
     * @param types     of the relationships to be included.
     * @return filtered relationships.
     */
    private Collection<Relationship> filterRelationships(Iterable<Relationship> relationships, Node node, Direction direction, RelationshipType[] types) {
        Set<String> typeNames = new HashSet<>();
        for (RelationshipType type : types) {
            typeNames.add(type.name());
        }

        Set<Relationship> result = new HashSet<>();
        for (Relationship r : relationships) {
            if ((typeNames.isEmpty() || typeNames.contains(r.getType().name())) &&
                    ((r.getStartNode().getId() == node.getId() && matches(r, r.getStartNode(), direction))
                            || (r.getEndNode().getId() == node.getId() && matches(r, r.getEndNode(), direction)))) {
                result.add(r);
            }
        }

        return result;
    }

    @Override
    protected Change<Relationship> createChangeObject(Relationship candidate) {
        return new Change<>(oldSnapshot(bugWorkaround(candidate)), newSnapshot(bugWorkaround(candidate)));
    }

    private Relationship bugWorkaround(Relationship relationship) {
        return relationship.getGraphDatabase().getRelationshipById(relationship.getId());
    }
}
