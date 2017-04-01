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
import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.PrefetchingIterator;

import java.util.Iterator;

/**
 * A {@link org.neo4j.graphdb.Relationship} {@link java.util.Iterator} decorator that filters out {@link org.neo4j.graphdb.Relationship}s not needed by the
 * {@link RelationshipInclusionPolicy} contained in the provided
 * {@link InclusionPolicies}.
 */
public class FilteredRelationshipIterator extends PrefetchingIterator<Relationship> implements Iterator<Relationship>, Iterable<Relationship> {

    private final Iterator<Relationship> wrappedIterator;
    private final InclusionPolicies policies;

    /**
     * Construct the iterator.
     *
     * @param wrappedIterable this decorates.
     * @param policies      for filtering.
     */
    public FilteredRelationshipIterator(Iterable<Relationship> wrappedIterable, InclusionPolicies policies) {
        this.wrappedIterator = wrappedIterable.iterator();
        this.policies = policies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Relationship> iterator() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Relationship fetchNextOrNull() {
        while (wrappedIterator.hasNext()) {
            Relationship next = wrappedIterator.next();
            if (!policies.getRelationshipInclusionPolicy().include(next)) {
                continue;
            }

            return new FilteredRelationship(next, policies);
        }

        return null;
    }
}
