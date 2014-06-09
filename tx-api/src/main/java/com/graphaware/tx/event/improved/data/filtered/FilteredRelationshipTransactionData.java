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

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.PropertyContainerInclusionStrategy;
import com.graphaware.common.strategy.PropertyInclusionStrategy;
import com.graphaware.tx.event.improved.data.PropertyContainerTransactionData;
import com.graphaware.tx.event.improved.data.RelationshipTransactionData;
import com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredRelationship;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collection;

/**
 * {@link FilteredPropertyContainerTransactionData} for {@link org.neo4j.graphdb.Relationship}s.
 */
public class FilteredRelationshipTransactionData extends FilteredPropertyContainerTransactionData<Relationship> implements RelationshipTransactionData {

    private final RelationshipTransactionData wrapped;

    /**
     * Construct filtered relationship transaction data.
     *
     * @param wrapped    wrapped relationship transaction data.
     * @param strategies for filtering.
     */
    public FilteredRelationshipTransactionData(RelationshipTransactionData wrapped, InclusionStrategies strategies) {
        super(strategies);
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyContainerTransactionData<Relationship> getWrapped() {
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship filtered(Relationship original) {
        return new FilteredRelationship(original, strategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getCreated(Node node, RelationshipType... types) {
        return filterPropertyContainers(wrapped.getCreated(node, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getCreated(Node node, Direction direction, RelationshipType... types) {
        return filterPropertyContainers(wrapped.getCreated(node, direction, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getDeleted(Node node, RelationshipType... types) {
        return filterPropertyContainers(wrapped.getDeleted(node, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getDeleted(Node node, Direction direction, RelationshipType... types) {
        return filterPropertyContainers(wrapped.getDeleted(node, direction, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyContainerInclusionStrategy<Relationship> getPropertyContainerInclusionStrategy() {
        return strategies.getRelationshipInclusionStrategy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyInclusionStrategy<Relationship> getPropertyInclusionStrategy() {
        return strategies.getRelationshipPropertyInclusionStrategy();
    }
}
