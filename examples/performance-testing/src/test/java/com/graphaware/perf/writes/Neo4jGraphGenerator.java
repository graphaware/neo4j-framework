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

package com.graphaware.perf.writes;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.module.algo.generator.BaseGraphGenerator;
import com.graphaware.module.algo.generator.config.GeneratorConfiguration;
import com.graphaware.module.algo.generator.relationship.RelationshipGenerator;
import com.graphaware.test.util.TestUtils;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.MultiThreadedBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.writer.neo4j.Neo4jWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.DeadlockDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link com.graphaware.module.algo.generator.GraphGenerator} for Neo4j.
 */
public class Neo4jGraphGenerator extends BaseGraphGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(Neo4jGraphGenerator.class);

    private final GraphDatabaseService database;
    private final int noThreads;
    private final AtomicInteger deadlocks = new AtomicInteger(0);
    private long time = 0;
    private Neo4jWriter writer;

    public Neo4jGraphGenerator(GraphDatabaseService database, int noThreads) {
        this.database = database;
        this.noThreads = noThreads;
    }

    public Neo4jGraphGenerator(GraphDatabaseService database, int noThreads, Neo4jWriter writer) {
        this.database = database;
        this.noThreads = noThreads;
        this.writer = writer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Long> generateNodes(final GeneratorConfiguration config) {
        final int numberOfNodes = config.getNumberOfNodes();

        final List<Long> nodes = new ArrayList<>();
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add((long) i);
        }

        time += TestUtils.time(new TestUtils.Timed() {
            @Override
            public void time() {
                new MultiThreadedBatchTransactionExecutor(new NoInputBatchTransactionExecutor(database, config.getBatchSize(), numberOfNodes, new UnitOfWork<NullItem>() {
                    @Override
                    public void execute(final GraphDatabaseService database, NullItem input, int batchNumber, int stepNumber) {
                        if (writer == null) {
                            config.getNodeCreator().createNode(database);
                        } else {
                            writer.write(new Runnable() {
                                @Override
                                public void run() {
                                    config.getNodeCreator().createNode(database);
                                }
                            });
                        }
                    }
                }), noThreads).execute();
            }
        });

        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateRelationships(final GeneratorConfiguration config, final List<Long> nodes) {
        final RelationshipGenerator<?> relationshipGenerator = config.getRelationshipGenerator();
        final List<SameTypePair<Integer>> relationships = relationshipGenerator.generateEdges();

        time += TestUtils.time(new TestUtils.Timed() {
            @Override
            public void time() {
                new MultiThreadedBatchTransactionExecutor(new IterableInputBatchTransactionExecutor<>(database, config.getBatchSize(), relationships, new UnitOfWork<SameTypePair<Integer>>() {
                    @Override
                    public void execute(final GraphDatabaseService database, final SameTypePair<Integer> input, int batchNumber, int stepNumber) {
                        try {
                            if (writer == null) {
                                final Node first = database.getNodeById(nodes.get(input.first()));
                                final Node second = database.getNodeById(nodes.get(input.second()));
                                config.getRelationshipCreator().createRelationship(first, second);
                            } else {
                                writer.write(new Runnable() {
                                    @Override
                                    public void run() {
                                        final Node first = database.getNodeById(nodes.get(input.first()));
                                        final Node second = database.getNodeById(nodes.get(input.second()));
                                        config.getRelationshipCreator().createRelationship(first, second);
                                    }
                                });
                            }
                        } catch (DeadlockDetectedException e) {
                            LOG.warn("Deadlock no. " + deadlocks.incrementAndGet());
                        }
                    }
                }), noThreads).execute();
            }
        });
    }

    public int getDeadlocks() {
        return deadlocks.get();
    }

    public long getTime() {
        return time;
    }
}
