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

package com.graphaware.tx.event.batch.propertycontainer;

import com.graphaware.common.wrapper.BasePropertyContainerWrapper;
import org.neo4j.graphdb.PropertyContainer;

/**
 * A proxy {@link org.neo4j.graphdb.PropertyContainer} to be stored in {@link com.graphaware.tx.event.batch.data.BatchTransactionData}
 * during batch insert operations.
 */
public abstract class BatchPropertyContainer<T extends PropertyContainer> extends BasePropertyContainerWrapper<T> {

    protected final long id;

    /**
     * Construct a new proxy property container.
     *
     * @param id of the container.
     */
    protected BatchPropertyContainer(long id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        throw new UnsupportedOperationException("Deletes are not supported by BatchInserters!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchPropertyContainer that = (BatchPropertyContainer) o;

        if (id != that.id) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
