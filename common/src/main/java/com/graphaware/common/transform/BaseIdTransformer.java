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
 * Abstract base-class for {@link IdTransformer} implementations.
 *
 * @param <ID> custom ID type.
 * @param <E>  entity type.
 */
public abstract class BaseIdTransformer<ID, E extends Entity> implements IdTransformer<ID, E> {

    @Override
    public final long toGraphId(ID id) {
        if (id == null) {
            return DetachedEntity.NEW;
        }

        return toExistingGraphId(id);
    }

    /**
     * Transform a custom ID to internal Neo4j ID.
     *
     * @param id to transform. Will never be <code>null</code>.
     * @return internal Neo4j ID.
     */
    protected abstract long toExistingGraphId(ID id);
}
