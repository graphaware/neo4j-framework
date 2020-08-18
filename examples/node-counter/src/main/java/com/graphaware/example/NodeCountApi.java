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

package com.graphaware.example;

import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

/**
 * Sample REST API for counting all nodes in the database.
 */
public class NodeCountApi {

    @Context
    public GraphDatabaseAPI database;

    @Procedure(mode = Mode.READ, name = "ga.example.nodeCount")
    public Stream<Output> count() {
        long count;

        try (Transaction tx = database.beginTx()) {
            count = Iterables.count(database.getAllNodes());
            tx.success();
        }

        return Stream.of(new Output(count));
    }

    public class Output {
        public Long out;

        public Output(Long out) {
            this.out = out;
        }
    }
}