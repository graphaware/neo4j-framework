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

package com.graphaware.common.policy.inclusion.none;

import com.graphaware.common.policy.inclusion.BaseEntityInclusionPolicy;
import com.graphaware.common.policy.inclusion.EntityInclusionPolicy;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Entity;
import org.neo4j.helpers.collection.Iterables;

/**
 * {@link EntityInclusionPolicy} that ignores all entities.
 */
public abstract class IncludeNoEntities<T extends Entity> extends BaseEntityInclusionPolicy<T> {

    protected IncludeNoEntities() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean include(T entity) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<T> doGetAll(GraphDatabaseService database) {
        return Iterables.empty();
    }
}
