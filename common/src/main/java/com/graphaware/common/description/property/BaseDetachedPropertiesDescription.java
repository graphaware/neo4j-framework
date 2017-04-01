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

package com.graphaware.common.description.property;

import com.graphaware.common.description.predicate.Predicate;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.graphaware.common.description.predicate.Predicates.equalTo;

/**
 * Base class for {@link DetachedPropertiesDescription} implementations.
 */
public abstract class BaseDetachedPropertiesDescription extends BasePropertiesDescription implements DetachedPropertiesDescription {

    protected final Map<String, Predicate> predicates = new TreeMap<>();

    /**
     * Construct a new properties description as the most specific description of the given property container.
     *
     * @param propertyContainer to construct the most specific properties description from.
     */
    protected BaseDetachedPropertiesDescription(PropertyContainer propertyContainer) {
        for (String key : propertyContainer.getPropertyKeys()) {
            predicates.put(key, equalTo(propertyContainer.getProperty(key)));
        }
    }

    /**
     * Construct a new properties description from the given map of predicates.
     *
     * @param predicates to copy.
     */
    protected BaseDetachedPropertiesDescription(Map<String, Predicate> predicates) {
        this.predicates.putAll(predicates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetachedPropertiesDescription with(String propertyKey, Predicate predicate) {
        Map<String, Predicate> newPredicates = new HashMap<>(predicates);
        newPredicates.put(propertyKey, predicate);
        return newInstance(newPredicates);
    }

    /**
     * Create a new instance of this class with the given predicates.
     *
     * @param predicates to copy.
     * @return new instance.
     */
    protected abstract DetachedPropertiesDescription newInstance(Map<String, Predicate> predicates);

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate get(String key) {
        if (!predicates.containsKey(key)) {
            return undefined();
        }

        return predicates.get(key);
    }

    /**
     * Get the default predicate for undefined keys.
     *
     * @return predicate.
     */
    protected abstract Predicate undefined();

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getKeys() {
        return predicates.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseDetachedPropertiesDescription that = (BaseDetachedPropertiesDescription) o;

        if (!predicates.equals(that.predicates)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return predicates.hashCode();
    }
}
