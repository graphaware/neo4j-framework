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

package com.graphaware.tx.event.improved.propertycontainer.filtered;

import com.graphaware.common.strategy.InclusionStrategies;
import com.graphaware.common.strategy.PropertyInclusionStrategy;
import com.graphaware.common.wrapper.BasePropertyContainerWrapper;
import org.neo4j.graphdb.PropertyContainer;

/**
 * A {@link org.neo4j.graphdb.PropertyContainer} decorator that transparently filters out properties and (where relevant) other containers
 * according to the provided {@link com.graphaware.common.strategy.InclusionStrategies}. Mutating operations are passed through to the decorated
 * {@link org.neo4j.graphdb.PropertyContainer} without modifications.
 */
public abstract class FilteredPropertyContainer<T extends PropertyContainer> extends BasePropertyContainerWrapper<T> {

    protected final T wrapped;
    protected final InclusionStrategies strategies;

    /**
     * Create a new filtering decorator.
     *
     * @param wrapped    decorated property container.
     * @param strategies for filtering.
     */
    protected FilteredPropertyContainer(T wrapped, InclusionStrategies strategies) {
        this.wrapped = wrapped;
        this.strategies = strategies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getWrapped() {
        return wrapped;
    }

    /**
     * Get appropriate property inclusion strategy.
     *
     * @return strategy.
     */
    protected abstract PropertyInclusionStrategy<T> getPropertyInclusionStrategy();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        if (!getPropertyInclusionStrategy().include(key, self())) {
            return false;
        }
        return super.hasProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        return new FilteredPropertyKeyIterator<>(super.getPropertyKeys(), self(), getPropertyInclusionStrategy());
    }
}
