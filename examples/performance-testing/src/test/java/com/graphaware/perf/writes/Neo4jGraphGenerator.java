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

package com.graphaware.perf.writes;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.test.util.TestUtils;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.MultiThreadedBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.input.NoInput;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.DeadlockDetectedException;
import org.neo4j.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Neo4jGraphGenerator {

    private static final Log LOG = LoggerFactory.getLogger(Neo4jGraphGenerator.class);

    private final GraphDatabaseService database;
    private final int noThreads;
    private final AtomicInteger deadlocks = new AtomicInteger(0);
    private long time = 0;
    private Neo4jWriter writer;
    private Random random = new Random();

    public Neo4jGraphGenerator(GraphDatabaseService database, int noThreads) {
        this.database = database;
        this.noThreads = noThreads;
    }

    public Neo4jGraphGenerator(GraphDatabaseService database, int noThreads, Neo4jWriter writer) {
        this.database = database;
        this.noThreads = noThreads;
        this.writer = writer;
    }

    public void generateGraph(int numberOfNodes) {
        List<Long> nodes = generateNodes(numberOfNodes);
        generateRelationships(nodes);
    }

    /**
     * {@inheritDoc}
     */
    protected List<Long> generateNodes(int numberOfNodes) {
        final List<Long> nodes = new ArrayList<>();
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add((long) i);
        }

        time += TestUtils.time(() -> new MultiThreadedBatchTransactionExecutor(new NoInputBatchTransactionExecutor(database, 1000, numberOfNodes, (database1, input, batchNumber, stepNumber) -> {
            if (writer == null) {
                createNode(database1, batchNumber, stepNumber);
            } else {
                writer.write(() -> createNode(database1, batchNumber, stepNumber));
            }
        }), noThreads).execute());

        return nodes;
    }

    private void createNode(GraphDatabaseService database1, int batchNumber, int stepNumber) {
        Node node = database1.createNode(Label.label("Test"));
        node.setProperty("name", "Node " + batchNumber + stepNumber);
    }

    protected void generateRelationships(final List<Long> nodes) {

        time += TestUtils.time(() -> new MultiThreadedBatchTransactionExecutor(new NoInputBatchTransactionExecutor(database, 1000, nodes.size() * 5, (database1, input, batchNumber, stepNumber) -> {
            try {
                if (writer == null) {
                    createRelationship(database1, nodes);
                } else {
                    writer.write(() -> createRelationship(database1, nodes));
                }
            } catch (DeadlockDetectedException e) {
                LOG.warn("Deadlock no. " + deadlocks.incrementAndGet());
            }
        }), noThreads).execute());
    }

    private void createRelationship(GraphDatabaseService database, List<Long> nodes) {
        final Node first = database.getNodeById(nodes.get(random.nextInt(nodes.size())));
        final Node second = database.getNodeById(nodes.get(random.nextInt(nodes.size())));
        first.createRelationshipTo(second, RelationshipType.withName("TEST"));
    }

    public int getDeadlocks() {
        return deadlocks.get();
    }

    public long getTime() {
        return time;
    }
}
