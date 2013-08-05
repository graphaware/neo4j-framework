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

package com.graphaware.tx.event.batch.propertycontainer.inserter;

import com.graphaware.tx.event.batch.propertycontainer.BatchPropertyContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.unsafe.batchinsert.BatchInserter;

/**
 * A {@link com.graphaware.tx.event.batch.propertycontainer.BatchPropertyContainer} to be used with {@link com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter} when performing
 * batch inserts.
 */
public abstract class BatchInserterPropertyContainer<T extends PropertyContainer> extends BatchPropertyContainer<T> {

    protected final BatchInserter batchInserter;

    /**
     * Construct a new proxy property container.
     *
     * @param id            of the container.
     * @param batchInserter currently used for batch inserts.
     */
    protected BatchInserterPropertyContainer(long id, BatchInserter batchInserter) {
        super(id);
        this.batchInserter = batchInserter;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always.
     */
    @Override
    public GraphDatabaseService getGraphDatabase() {
        throw new UnsupportedOperationException("BatchInserter does not operate on GraphDatabaseService!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getWrapped() {
        throw new UnsupportedOperationException("There is no wrapped Node/Relationship in BatchInserterNode. All operations must" +
                " be overridden as it talks to a different API from GraphDatabaseService. This is a bug.");
    }
}
