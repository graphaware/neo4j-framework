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

package com.graphaware.common.policy.inclusion;

import org.neo4j.graphdb.Entity;

/**
 * {@link InclusionPolicy} deciding whether to include properties of some {@link Entity} or not.
 *
 * @param <T> type of the entity.
 */
public interface PropertyInclusionPolicy<T extends Entity> extends InclusionPolicy {

    /**
     * Should a property with the given key of the given entity be included?
     *
     * @param key               of the property.
     * @param entity containing the property.
     * @return true iff the property should be included.
     */
    boolean include(String key, T entity);
}
