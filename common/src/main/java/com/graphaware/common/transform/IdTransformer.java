/*
 * Copyright (c) 2013-2020 GraphAware
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

package com.graphaware.common.transform;

import com.graphaware.common.representation.DetachedEntity;
import org.neo4j.graphdb.Entity;

/**
 * A transformer of custom IDs to and from internal Neo4j {@link Entity} IDs.
 *
 * @param <ID> custom ID type.
 * @param <E>  entity type.
 */
public interface IdTransformer<ID, E extends Entity> {

    /**
     * Transform a custom ID to internal Neo4j ID.
     *
     * @param id to transform. Can be <code>null</code>, which represents a new entity not yet persisted to the database.
     * @return internal Neo4j ID. {@link DetachedEntity#NEW} should be returned in case the id parameter is <code>null</code>.
     */
    long toGraphId(ID id);

    /**
     * Transform an internal Neo4j node ID of a {@link Entity} to a custom ID.
     *
     * @param entity to take the ID to transform from. Must not be <code>null</code>.
     * @return transformed custom ID. Never <code>null</code>.
     */
    ID fromEntity(E entity);
}
