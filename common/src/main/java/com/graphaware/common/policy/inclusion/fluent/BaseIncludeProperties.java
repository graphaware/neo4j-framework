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

package com.graphaware.common.policy.inclusion.fluent;

import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.PropertyContainer;

/**
 * Abstract base class for {@link PropertyInclusionPolicy} implementations with fluent interface,
 * intended to be used programmatically.
 */
public abstract class BaseIncludeProperties<T extends BaseIncludeProperties<T, P>, P extends PropertyContainer> implements PropertyInclusionPolicy<P> {

    private final String key;

    /**
     * Create a new policy.
     *
     * @param key that matching properties must have, can be null for all properties.
     */
    public BaseIncludeProperties(String key) {
        this.key = key;
    }

    /**
     * Create a new policy from the current one, reconfigured to only match properties with the given key.
     *
     * @param key that matching properties must have, can be null for all properties.
     * @return reconfigured policy.
     */
    public T with(String key) {
        if (key == null) {
            return newInstance(null);
        }

        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Empty keys are not supported");
        }

        return newInstance(key);
    }

    /**
     * Create a new instance of this policy with the given key.
     *
     * @param key of the new policy.
     * @return new policy.
     */
    protected abstract T newInstance(String key);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(String key, P propertyContainer) {
        return this.key == null || this.key.equals(key);
    }

    public String getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseIncludeProperties that = (BaseIncludeProperties) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
