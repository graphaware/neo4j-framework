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

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;

import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.graphdb.RelationshipType.withName;


/**
 * Demo of {@link ChangeLogger}.
 */
@ExtendWith(Neo4jExtension.class)
public class ChangeLoggerDemo {

    @InjectNeo4j
    private Neo4j neo4j;
    @InjectNeo4j
    private GraphDatabaseService database;

    private ChangeLogger listener;

    @BeforeEach
    public void setUp() throws Exception {
        listener = new ChangeLogger();
        neo4j.databaseManagementService().registerTransactionEventListener(database.databaseName(), listener);
    }

    @AfterEach
    public void tearDown() {
        neo4j.databaseManagementService().unregisterTransactionEventListener(database.databaseName(), listener);
    }

    @Test
    public void demonstrateLogging() {
        performMutations(database);
    }

    private void performMutations(GraphDatabaseService database) {
        SimpleTransactionExecutor executor = new SimpleTransactionExecutor(database);

        //create nodes
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(Transaction database) {
                database.createNode(label("SomeLabel1"));

                Node node1 = database.createNode(label("SomeLabel2"));
                node1.setProperty("name", "One");

                Node node2 = database.createNode();
                node2.setProperty("name", "Two");

                Node node3 = database.createNode();
                node3.setProperty("name", "Three");
            }
        });

        //create relationship
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(Transaction tx) {
                Node one = tx.getNodeById(1);
                Node two = tx.getNodeById(2);

                Relationship relationship = one.createRelationshipTo(two, withName("TEST"));
                relationship.setProperty("level", 2);

                two.createRelationshipTo(one, withName("TEST"));
            }
        });

        //change and delete nodes
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(Transaction tx) {
                tx.getNodeById(3).delete();
                tx.getNodeById(1).setProperty("name", "New One");
                tx.getNodeById(1).setProperty("numbers", new int[]{1, 2, 3});
                tx.getNodeById(2).addLabel(label("NewLabel"));
            }
        });

        //change and delete relationships
        executor.executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(Transaction tx) {
                tx.getNodeById(2).getSingleRelationship(withName("TEST"), OUTGOING).delete();

                tx.getNodeById(1).getSingleRelationship(withName("TEST"), OUTGOING).setProperty("level", 3);
            }
        });
    }
}
