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

package com.graphaware.tx.executor.batch;

import com.graphaware.tx.executor.single.TransactionCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.graphaware.common.util.IterableUtils.countNodes;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor}.
 */
public class IterableInputBatchTransactionExecutorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void nodesShouldBeCreatedFromListOfNames() {
        List<String> nodeNames = Arrays.asList("Name1", "Name2", "Name3");

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, 2, nodeNames, new UnitOfWork<String>() {
            @Override
            public void execute(GraphDatabaseService database, String nodeName, int batchNumber, int stepNumber) {
                Node node = database.createNode();
                node.setProperty("name", nodeName + batchNumber + stepNumber);
            }
        });

        executor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(3, countNodes(database));
            assertEquals("Name111", database.getNodeById(0).getProperty("name"));
            assertEquals("Name212", database.getNodeById(1).getProperty("name"));
            assertEquals("Name321", database.getNodeById(2).getProperty("name"));
        }
    }

    @Test
    public void iterableAcquiredInTransactionShouldBeProcessed() {
        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < 100; i++) {
                database.createNode();
            }
            tx.success();
        }

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, 10,
                new TransactionCallback<Iterable<Node>>() {
                    @Override
                    public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                        return GlobalGraphOperations.at(database).getAllNodes();
                    }
                },
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        node.setProperty("name", "Name" + batchNumber + stepNumber);
                    }
                }
        );

        executor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals("Name11", database.getNodeById(0).getProperty("name"));
            assertEquals("Name12", database.getNodeById(1).getProperty("name"));
            assertEquals("Name13", database.getNodeById(2).getProperty("name"));
            assertEquals("Name108", database.getNodeById(97).getProperty("name"));
            assertEquals("Name109", database.getNodeById(98).getProperty("name"));
            assertEquals("Name1010", database.getNodeById(99).getProperty("name"));
        }
    }

    @Test
    public void bugTest() {
        final Label label = DynamicLabel.label("TEST");

        try (Transaction tx = database.beginTx()) {
            database.createNode(label);
            database.createNode(label);
            tx.success();
        }

        final AtomicInteger count = new AtomicInteger(0);

        new IterableInputBatchTransactionExecutor<>(database, 100, new TransactionCallback<Iterable<Node>>() {
            @Override
            public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                return GlobalGraphOperations.at(database).getAllNodesWithLabel(label);
            }
        }, new UnitOfWork<Node>() {
            @Override
            public void execute(GraphDatabaseService database, Node input, int batchNumber, int stepNumber) {
                count.incrementAndGet();
            }
        }
        ).execute();

        assertEquals(2, count.get());
    }
}
