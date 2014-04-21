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

package com.graphaware.module.relcount.compact;

import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.cache.DegreeCachingNode;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import com.graphaware.tx.executor.single.TransactionExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.predicate.Predicates.any;
import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;


/**
 * Integration test for compactions.
 */
public class CompactionIntegrationTest {

    private GraphDatabaseService database;
    private TransactionExecutor executor;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            database.createNode(); //ID = 0
            tx.success();
        }

        executor = new SimpleTransactionExecutor(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void nothingShouldBeCompactedBeforeThresholdIsReached() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(4);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")), 14, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v3")), 2, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v4")), 3, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(4, node.getCachedDegrees().size());
        assertEquals(14, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", equalTo("v1"))));
        assertEquals(1, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", equalTo("v2"))));
        assertEquals(2, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", equalTo("v3"))));
        assertEquals(3, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", equalTo("v4"))));
    }

    @Test
    public void countShouldBeCompactedWhenThresholdIsReached() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(4);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")), 14, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v3")), 2, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v4")), 3, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v5")), 4, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(1, node.getCachedDegrees().size());
        assertEquals(24, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", any())));
    }

    @Test
    public void verifyMultipleCompactions() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(4);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v3")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v4")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v3")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v4")), 1, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(2, node.getCachedDegrees().size());
        assertEquals(4, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", any())));
        assertEquals(4, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", equalTo("v2")).with("k2", any())));
    }

    @Test
    public void verifyMultiLevelCompaction() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(4);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("test", OUTGOING).with("z1", equalTo("v1")).with("k2", equalTo("v1")).with("k3", equalTo("v1")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("z1", equalTo("v1")).with("k2", equalTo("v2")).with("k3", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("z1", equalTo("v1")).with("k2", equalTo("v3")).with("k3", equalTo("v3")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("z1", equalTo("v1")).with("k2", equalTo("v4")).with("k3", equalTo("v4")), 1, true);
                node.incrementDegree(literal("test", OUTGOING).with("z1", equalTo("v1")).with("k2", equalTo("v5")).with("k3", equalTo("v5")), 1, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(1, node.getCachedDegrees().size());
        assertEquals(5, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("z1", equalTo("v1")).with("k2", any()).with("k3", any())));
    }

    @Test
    public void verifyImpossibleCompaction() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(4);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")).with("k3", equalTo("v1")), 1);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v2")).with("k3", equalTo("v2")), 1);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v3")).with("k3", equalTo("v3")), 1);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v4")).with("k3", equalTo("v4")), 1);
                node.incrementDegree(literal("test", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v5")).with("k3", equalTo("v5")), 1);

                node.incrementDegree(literal("test", OUTGOING).with("k2", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test2", OUTGOING).with("k2", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test3", OUTGOING).with("k2", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test4", OUTGOING).with("k2", equalTo("v2")), 1, true);
                node.incrementDegree(literal("test5", OUTGOING).with("k2", equalTo("v2")), 1, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(5, node.getCachedDegrees().size());
        assertEquals(6, (int) node.getCachedDegrees().get(literal("test", OUTGOING).with("k1", any()).with("k2", any()).with("k3", any())));
        assertEquals(1, (int) node.getCachedDegrees().get(literal("test2", OUTGOING).with("k2", equalTo("v2"))));
        assertEquals(1, (int) node.getCachedDegrees().get(literal("test3", OUTGOING).with("k2", equalTo("v2"))));
        assertEquals(1, (int) node.getCachedDegrees().get(literal("test4", OUTGOING).with("k2", equalTo("v2"))));
        assertEquals(1, (int) node.getCachedDegrees().get(literal("test5", OUTGOING).with("k2", equalTo("v2"))));
    }

    @Test
    public void compactionIncludingWildcards() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(1);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("ONE", INCOMING).with("k1", equalTo("v1")).with("k2", equalTo("v2")), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("k1", any()).with("w", any()), 2, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(1, node.getCachedDegrees().size());
        assertEquals(3, (int) node.getCachedDegrees().get(literal("ONE", INCOMING).with("k1", any()).with("w", any()).with("k2", any())));
    }

    @Test
    public void compactionIncludingWildcards2() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(1);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("ONE", INCOMING).with("k1", any()), 1);
                node.incrementDegree(literal("ONE", INCOMING).with("k2", any()), 2);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(1, node.getCachedDegrees().size());
        assertEquals(3, (int) node.getCachedDegrees().get(literal("ONE", INCOMING).with("k1", any()).with("k2", any())));
    }

    @Test
    public void anotherCompactionSmokeTest() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(9);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(1)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(2)).with("timestamp", equalTo(2)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(0)).with("timestamp", equalTo(3)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(4)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(0)).with("timestamp", equalTo(5)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(0)).with("timestamp", equalTo(6)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(7)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(2)).with("timestamp", equalTo(8)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(2)).with("timestamp", equalTo(9)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(10)), 1, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        if (node.getCachedDegrees().size() != 7) {
            System.out.println("NOW");
        }
        assertEquals(7, node.getCachedDegrees().size());
    }

    @Test
    public void anotherCompactionSmokeTest2() {
        final CompactionStrategy compactionStrategy = new ThresholdBasedCompactionStrategy(3);

        DegreeCachingNode node = executor.executeInTransaction(new TransactionCallback<DegreeCachingNode>() {
            @Override
            public DegreeCachingNode doInTransaction(GraphDatabaseService database) {
                DegreeCachingNode node = new DegreeCachingNode(
                        database.getNodeById(0), "TEST",
                        RelationshipCountConfigurationImpl.defaultConfiguration().with(compactionStrategy));

                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(1)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(2)).with("timestamp", equalTo(2)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(0)).with("timestamp", equalTo(3)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(4)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(0)).with("timestamp", equalTo(5)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(0)).with("timestamp", equalTo(6)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(7)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(2)).with("timestamp", equalTo(8)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(2)).with("timestamp", equalTo(9)), 1, true);
                node.incrementDegree(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", equalTo(10)), 1, true);

                compactionStrategy.compactRelationshipCounts(node);

                node.flush();

                return node;
            }
        });

        assertEquals(3, node.getCachedDegrees().size());
        assertEquals(3, (int) node.getCachedDegrees().get(literal("ONE", INCOMING).with("level", equalTo(0)).with("timestamp", any())));
        assertEquals(4, (int) node.getCachedDegrees().get(literal("ONE", INCOMING).with("level", equalTo(1)).with("timestamp", any())));
        assertEquals(3, (int) node.getCachedDegrees().get(literal("ONE", INCOMING).with("level", equalTo(2)).with("timestamp", any())));
    }
}
