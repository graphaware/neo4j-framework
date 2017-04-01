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

package com.graphaware.tx.event.improved.propertycontainer.filtered;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.wrapper.NodeWrapper;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * {@link FilteredPropertyContainer} which is a {@link org.neo4j.graphdb.Node}.
 */
public class FilteredNode extends FilteredPropertyContainer<Node> implements Node, NodeWrapper {

    /**
     * Create a new filtering node decorator.
     *
     * @param wrapped    decorated node.
     * @param policies for filtering.
     */
    public FilteredNode(Node wrapped, InclusionPolicies policies) {
        super(wrapped, policies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node self() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertyInclusionPolicy<Node> getPropertyInclusionPolicy() {
        return policies.getNodePropertyInclusionPolicy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Relationship> wrapRelationships(Iterable<Relationship> relationships, Direction direction, RelationshipType... relationshipTypes) {
        return new FilteredRelationshipIterator(relationships, policies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship wrapRelationship(Relationship relationship) {
        return new FilteredRelationship(relationship, policies);
    }
}
