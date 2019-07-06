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

package com.graphaware.common.description.property;

import com.graphaware.common.description.predicate.Predicate;
import org.neo4j.graphdb.Entity;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;

/**
 * The most specific {@link PropertiesDescription} of a {@link Entity} that lazily consults the underlying
 * {@link Entity} and returns only predicates of type {@link com.graphaware.common.description.predicate.EqualTo}.
 * For keys that don't have a corresponding property defined on the {@link Entity},
 * {@link com.graphaware.common.description.predicate.Undefined} is returned.
 */
public class LazyPropertiesDescription extends BasePropertiesDescription implements PropertiesDescription {

    private final Entity entity;

    /**
     * Construct a new properties description as the most specific description of the given entity.
     *
     * @param entity to construct the most specific properties description from.
     */
    public LazyPropertiesDescription(Entity entity) {
        this.entity = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate get(String key) {
        Object value = entity.getProperty(key, null);

        if (value == null) {
            return undefined();
        }

        return equalTo(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getKeys() {
        return entity.getPropertyKeys();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LazyPropertiesDescription that = (LazyPropertiesDescription) o;

        if (!entity.equals(that.entity)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}
