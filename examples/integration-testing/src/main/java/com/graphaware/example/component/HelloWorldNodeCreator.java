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

package com.graphaware.example.component;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * Very powerful class capable of creating a "Hello World" node. Intended for
 * demonstrating Neo4j integration testing with GraphAware Framework.
 */
public class HelloWorldNodeCreator {

    private final GraphDatabaseService database;

    public HelloWorldNodeCreator(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Create a hello world node.
     *
     * @return created node.
     */
    public Node createHelloWorldNode() {
        Node node;

        try (Transaction tx = database.beginTx()) {
            node = database.createNode(Label.label("HelloWorld"));
            node.setProperty("hello", "world");
            tx.success();
        }

        return node;
    }
}
