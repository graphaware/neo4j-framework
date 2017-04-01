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

package com.graphaware.test.data;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 * A {@link com.graphaware.test.data.DatabasePopulator} that populates Neo4j using a single transaction.
 */
public abstract class SingleTransactionPopulator implements DatabasePopulator {

    @Override
    public final void populate(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {
            doPopulate(database);
            tx.success();
        }
    }

    /**
     * Perform the database population. No need to handle transactions.
     *
     * @param database to populate.
     */
    protected abstract void doPopulate(GraphDatabaseService database);
}
