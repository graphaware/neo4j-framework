/*
 * Copyright (c) 2013-2018 GraphAware
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

package com.graphaware.common.representation;

import com.graphaware.common.expression.EntityExpressions;
import org.neo4j.graphdb.Entity;

import java.util.Map;

public abstract class AttachedEntity<T extends Entity> implements EntityExpressions {

    protected final T entity;

    public AttachedEntity(T entity) {
        this.entity = entity;
    }
    @Override
    public Map<String, Object> getProperties() {
        return entity.getAllProperties();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachedEntity<?> that = (AttachedEntity<?>) o;

        return entity != null ? entity.equals(that.entity) : that.entity == null;

    }

    @Override
    public int hashCode() {
        return entity != null ? entity.hashCode() : 0;
    }
}
