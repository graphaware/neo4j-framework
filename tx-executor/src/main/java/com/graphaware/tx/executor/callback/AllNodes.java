/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.tx.executor.callback;

import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * {@link TransactionCallback} returning all nodes in the database. Singleton.
 */
public final class AllNodes implements TransactionCallback<Iterable<Node>> {

    private static final AllNodes INSTANCE = new AllNodes();

    /**
     * Get an instance of this callback.
     *
     * @return instance.
     */
    public static AllNodes getInstance() {
        return INSTANCE;
    }

    private AllNodes() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
        return GlobalGraphOperations.at(database).getAllNodes();
    }
}
