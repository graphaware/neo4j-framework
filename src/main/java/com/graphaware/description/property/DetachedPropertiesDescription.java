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

package com.graphaware.description.property;

import com.graphaware.description.predicate.Predicate;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.description.predicate.Predicates.equalTo;

/**
 * A {@link com.graphaware.description.property.PropertiesDescription} that is immutable and maintains all its data;
 * thus, it can be serialized and stored. It also allows for generating new instances with different predicates, by
 * implementing the {@link FluentPropertiesDescription} interface.
 */
public abstract class DetachedPropertiesDescription extends BasePropertiesDescription implements FluentPropertiesDescription {

    protected final Map<String, Predicate> predicates = new HashMap<>();

    /**
     * Construct a new properties description as the most specific description of the given property container.
     *
     * @param propertyContainer to construct the most specific properties description from.
     */
    protected DetachedPropertiesDescription(PropertyContainer propertyContainer) {
        for (String key : propertyContainer.getPropertyKeys()) {
            predicates.put(key, equalTo(propertyContainer.getProperty(key)));
        }
    }

    /**
     * Construct a new properties description from the given map of predicates.
     *
     * @param predicates to copy.
     */
    protected DetachedPropertiesDescription(Map<String, Predicate> predicates) {
        this.predicates.putAll(predicates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FluentPropertiesDescription with(String propertyKey, Predicate predicate) {
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
    protected abstract FluentPropertiesDescription newInstance(Map<String, Predicate> predicates);

    /**
     * {@inheritDoc}
     */
    @Override
    protected PropertiesDescription self() {
        return this;
    }

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
}
