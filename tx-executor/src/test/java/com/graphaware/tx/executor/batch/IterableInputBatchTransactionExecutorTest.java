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

package com.graphaware.tx.executor.batch;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.tx.executor.input.AllNodes;
import com.graphaware.tx.executor.input.AllNodesWithLabel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import javax.ws.rs.core.Link;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.graphaware.common.util.IterableUtils.countNodes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor}.
 */
@ExtendWith(Neo4jExtension.class)
public class IterableInputBatchTransactionExecutorTest {

    @InjectNeo4j
    protected GraphDatabaseService database;

    @Test
    public void nodesShouldBeCreatedFromListOfNames() {
        List<String> nodeNames = Arrays.asList("Name1", "Name2", "Name3");
        final List<Long> ids = new LinkedList<>();

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, 2, nodeNames, new UnitOfWork<String>() {
            @Override
            public void execute(Transaction tx, String nodeName, int batchNumber, int stepNumber) {
                Node node = tx.createNode();
                node.setProperty("name", nodeName + batchNumber + stepNumber);
                ids.add(node.getId());
            }
        });

        executor.execute();

        try (Transaction tx = database.beginTx()) {
            assertEquals(3, countNodes(tx));
            assertEquals("Name111", tx.getNodeById(ids.get(0)).getProperty("name"));
            assertEquals("Name212", tx.getNodeById(ids.get(1)).getProperty("name"));
            assertEquals("Name321", tx.getNodeById(ids.get(2)).getProperty("name"));
        }
    }

    @Test
    public void iterableAcquiredInTransactionShouldBeProcessed() {
        final List<Long> ids = new LinkedList<>();

        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < 100; i++) {
                Node n = tx.createNode();
                ids.add(n.getId());
            }
            tx.commit();
        }

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, 10,
                new AllNodes(database, 10),
                new NodeUnitOfWork() {
                    @Override
                    public void execute(Node node, int batchNumber, int stepNumber) {
                        node.setProperty("name", "Name" + batchNumber + stepNumber);
                    }
                }
        );

        executor.execute();

        Set<String> remaining = new HashSet<>();
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                remaining.add("Name" + i + "" + j);
            }
        }

        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < 100; i++) {
                remaining.remove(tx.getNodeById(ids.get(i)).getProperty("name"));
            }
        }

        assertTrue(remaining.isEmpty());
    }

    @Test
    public void iterorAcquiredInTransactionShouldBeProcessed() {
        final List<Long> ids = new LinkedList<>();

        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < 100; i++) {
                Node n = tx.createNode(Label.label("Test"));
                ids.add(n.getId());
            }
            tx.commit();
        }

        BatchTransactionExecutor executor = new IterableInputBatchTransactionExecutor<>(database, 10,
                new AllNodesWithLabel(database, 10, Label.label("Test")),
                new NodeUnitOfWork() {
                    @Override
                    protected void execute(Node node, int batchNumber, int stepNumber) {
                        node.setProperty("name", "Name" + batchNumber + stepNumber);
                    }
                }
        );

        executor.execute();

        Set<String> remaining = new HashSet<>();
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                remaining.add("Name" + i + "" + j);
            }
        }

        try (Transaction tx = database.beginTx()) {
            for (int i = 0; i < 100; i++) {
                remaining.remove(tx.getNodeById(ids.get(i)).getProperty("name"));
            }
        }

        assertTrue(remaining.isEmpty());
    }

    @Test
    public void bugTest() {
        final Label label = Label.label("TEST");

        try (Transaction tx = database.beginTx()) {
            tx.createNode(label);
            tx.createNode(label);
            tx.commit();
        }

        final AtomicInteger count = new AtomicInteger(0);

        new IterableInputBatchTransactionExecutor<>(database, 100, new AllNodesWithLabel(database, 100, label),
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(Transaction tx, Node input, int batchNumber, int stepNumber) {
                        count.incrementAndGet();
                    }
                }
        ).execute();

        assertEquals(2, count.get());
    }
}
