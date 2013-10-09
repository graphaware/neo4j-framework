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

import java.util.Map;

import static com.graphaware.description.predicate.Predicates.any;

/**
 * A {@link BaseDetachedPropertiesDescription} where every predicate not explicitly defined is
 * {@link com.graphaware.description.predicate.Any}.
 */
public class WildcardPropertiesDescription extends BaseDetachedPropertiesDescription {

    /**
     * Construct a new properties description as the most specific description of the given property container.
     *
     * @param propertyContainer to construct the most specific properties description from.
     */
    public WildcardPropertiesDescription(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a new properties description from the given map of predicates.
     *
     * @param predicates predicates.
     */
    public WildcardPropertiesDescription(Map<String, Predicate> predicates) {
        super(predicates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DetachedPropertiesDescription newInstance(Map<String, Predicate> predicates) {
        return new WildcardPropertiesDescription(predicates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Predicate undefined() {
        return any();
    }

    //todo is more general than = always if empty
}
