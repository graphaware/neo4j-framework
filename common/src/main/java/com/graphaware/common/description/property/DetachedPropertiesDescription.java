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

/**
 * A {@link PropertiesDescription} that must be detached from the database, i.e. store its own data internally rather
 * than referring to an underlying {@link org.neo4j.graphdb.PropertyContainer}. It is immutable; once instantiated,
 * new instances with different {@link Predicate}s can be constructed using the {@link #with(String, Predicate)} method.
 */
public interface DetachedPropertiesDescription extends PropertiesDescription {

    /**
     * Construct a new description from this description by adding/replacing a predicate with a new one.
     *
     * @param propertyKey key of the property the predicate is for.
     * @param predicate   the predicate.
     * @return a new instance of properties description.
     */
    DetachedPropertiesDescription with(String propertyKey, Predicate predicate);
}
