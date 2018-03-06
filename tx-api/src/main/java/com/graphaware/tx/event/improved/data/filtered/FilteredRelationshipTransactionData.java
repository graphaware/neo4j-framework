/*
 * Copyright (c) 2013-2018 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.tx.event.improved.data.filtered;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.EntityInclusionPolicy;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.tx.event.improved.data.EntityTransactionData;
import com.graphaware.tx.event.improved.data.RelationshipTransactionData;
import com.graphaware.tx.event.improved.entity.filtered.FilteredRelationship;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collection;

/**
 * {@link FilteredEntityTransactionData} for {@link org.neo4j.graphdb.Relationship}s.
 */
public class FilteredRelationshipTransactionData extends FilteredEntityTransactionData<Relationship> implements RelationshipTransactionData {

    private final RelationshipTransactionData wrapped;

    /**
     * Construct filtered relationship transaction data.
     *
     * @param wrapped    wrapped relationship transaction data.
     * @param policies for filtering.
     */
    public FilteredRelationshipTransactionData(RelationshipTransactionData wrapped, InclusionPolicies policies) {
        super(policies);
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EntityTransactionData<Relationship> getWrapped() {
        return wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship filtered(Relationship original) {
        return new FilteredRelationship(original, policies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getCreated(Node node, RelationshipType... types) {
        return filterEntities(wrapped.getCreated(node, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getCreated(Node node, Direction direction, RelationshipType... types) {
        return filterEntities(wrapped.getCreated(node, direction, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getDeleted(Node node, RelationshipType... types) {
        return filterEntities(wrapped.getDeleted(node, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Relationship> getDeleted(Node node, Direction direction, RelationshipType... types) {
        return filterEntities(wrapped.getDeleted(node, direction, types));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EntityInclusionPolicy<Relationship> getEntityInclusionPolicy() {
        return policies.getRelationshipInclusionPolicy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyInclusionPolicy<Relationship> getPropertyInclusionPolicy() {
        return policies.getRelationshipPropertyInclusionPolicy();
    }
}
