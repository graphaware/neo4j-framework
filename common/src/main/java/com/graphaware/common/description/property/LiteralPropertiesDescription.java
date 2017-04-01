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
import com.graphaware.common.description.predicate.Predicates;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

/**
 * A {@link BaseDetachedPropertiesDescription} where every predicate not explicitly defined is
 * {@link com.graphaware.common.description.predicate.Undefined}.
 */
public class LiteralPropertiesDescription extends BaseDetachedPropertiesDescription {

    /**
     * Construct a new properties description as the most specific description of the given property container.
     *
     * @param propertyContainer to construct the most specific properties description from.
     */
    public LiteralPropertiesDescription(PropertyContainer propertyContainer) {
        super(propertyContainer);
    }

    /**
     * Construct a new properties description from the given map of predicates.
     *
     * @param predicates predicates.
     */
    public LiteralPropertiesDescription(Map<String, Predicate> predicates) {
        super(predicates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected LiteralPropertiesDescription newInstance(Map<String, Predicate> predicates) {
        return new LiteralPropertiesDescription(predicates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Predicate undefined() {
        return Predicates.undefined();
    }
}
