/*
 * Copyright (c) 2013-2019 GraphAware
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

package com.graphaware.tx.event.improved.entity.filtered;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.wrapper.BaseEntityWrapper;
import org.neo4j.graphdb.Entity;

/**
 * A {@link org.neo4j.graphdb.Entity} decorator that transparently filters out properties and (where relevant) other entities
 * according to the provided {@link InclusionPolicies}. Mutating operations are passed through to the decorated
 * {@link org.neo4j.graphdb.Entity} without modifications.
 */
public abstract class FilteredEntity<T extends Entity> extends BaseEntityWrapper<T> {

    protected final T wrapped;
    protected final InclusionPolicies policies;

    /**
     * Create a new filtering decorator.
     *
     * @param wrapped    decorated entity.
     * @param policies for filtering.
     */
    protected FilteredEntity(T wrapped, InclusionPolicies policies) {
        this.wrapped = wrapped;
        this.policies = policies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getWrapped() {
        return wrapped;
    }

    /**
     * Get appropriate property inclusion policy.
     *
     * @return policy.
     */
    protected abstract PropertyInclusionPolicy<T> getPropertyInclusionPolicy();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        if (!getPropertyInclusionPolicy().include(key, self())) {
            return false;
        }
        return super.hasProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        return new FilteredPropertyKeyIterator<>(super.getPropertyKeys(), self(), getPropertyInclusionPolicy());
    }
}
