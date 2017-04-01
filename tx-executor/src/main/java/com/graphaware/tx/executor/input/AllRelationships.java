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

package com.graphaware.tx.executor.input;

import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

/**
 * {@link TransactionalInput} returning all relationships in the database.
 */
public final class AllRelationships extends TransactionalInput<Relationship> {

    /**
     * Create a new input.
     *
     * @param database  to take all relationships from.
     * @param batchSize how many relationships in a batch.
     */
    public AllRelationships(GraphDatabaseService database, int batchSize) {
        super(database, batchSize, GraphDatabaseService::getAllRelationships);
    }
}
