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

import com.graphaware.common.description.MutuallyExclusive;
import com.graphaware.common.description.PartiallyComparable;
import com.graphaware.common.description.predicate.Predicate;

/**
 * An immutable description of properties, i.e. a map of predicates (property constraints) keyed by a String property name.
 * It is {@link PartiallyComparable} and can judge, whether it is {@link MutuallyExclusive} with another one.
 */
public interface PropertiesDescription extends PartiallyComparable<PropertiesDescription>, MutuallyExclusive<PropertiesDescription> {

    /**
     * Get predicate for a property with the given key.
     *
     * @param key key.
     * @return predicate. Never null.
     */
    Predicate get(String key);

    /**
     * Get keys of all predicates.
     *
     * @return all keys.
     */
    Iterable<String> getKeys();
}
