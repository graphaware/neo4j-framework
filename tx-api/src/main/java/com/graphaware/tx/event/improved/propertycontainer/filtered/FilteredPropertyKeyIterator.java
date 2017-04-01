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

import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.helpers.collection.PrefetchingIterator;

import java.util.Iterator;

/**
 * A property key {@link java.util.Iterator} decorator that filters out keys not needed by the contained {@link PropertyInclusionPolicy}.
 */
public class FilteredPropertyKeyIterator<T extends PropertyContainer> extends PrefetchingIterator<String> implements Iterable<String> {

    private final Iterator<String> wrappedIterator;
    private final T wrappedPropertyContainer;
    private final PropertyInclusionPolicy<T> propertyInclusionPolicy;

    /**
     * Construct the iterator.
     *
     * @param wrappedIterator           wrapped iterator that this decorates (and filters).
     * @param wrappedPropertyContainer  property container that the iterator belongs to.
     * @param propertyInclusionPolicy policy used for filtering.
     */
    public FilteredPropertyKeyIterator(Iterable<String> wrappedIterator, T wrappedPropertyContainer, PropertyInclusionPolicy<T> propertyInclusionPolicy) {
        this.wrappedIterator = wrappedIterator.iterator();
        this.wrappedPropertyContainer = wrappedPropertyContainer;
        this.propertyInclusionPolicy = propertyInclusionPolicy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String fetchNextOrNull() {
        while (wrappedIterator.hasNext()) {
            String key = wrappedIterator.next();
            if (propertyInclusionPolicy.include(key, wrappedPropertyContainer)) {
                return key;
            }
        }

        return null;
    }
}
