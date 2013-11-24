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

package com.graphaware.tx.event.batch.propertycontainer.database;

import com.graphaware.tx.event.batch.propertycontainer.BatchPropertyContainer;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase;

/**
 * A {@link com.graphaware.tx.event.batch.propertycontainer.BatchPropertyContainer} to be used with
 * {@link org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase} when performing batch inserts.
 */
public abstract class BatchDatabasePropertyContainer<T extends PropertyContainer> extends BatchPropertyContainer<T> {

    protected final TransactionSimulatingBatchGraphDatabase database;

    /**
     * Construct a new proxy property container.
     *
     * @param id       of the container.
     * @param database currently used for batch inserts.
     */
    protected BatchDatabasePropertyContainer(long id, TransactionSimulatingBatchGraphDatabase database) {
        super(id);
        this.database = database;
    }
}
