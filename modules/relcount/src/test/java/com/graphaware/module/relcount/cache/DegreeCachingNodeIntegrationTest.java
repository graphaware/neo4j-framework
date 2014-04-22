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

package com.graphaware.module.relcount.cache;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.common.description.serialize.Serializer;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.RelationshipCountRuntimeModule;
import com.graphaware.module.relcount.count.CachedRelationshipCounter;
import com.graphaware.module.relcount.count.RelationshipCounter;
import com.graphaware.runtime.BaseGraphAwareRuntime;
import com.graphaware.runtime.NeedsInitializationException;
import com.graphaware.runtime.ProductionGraphAwareRuntime;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionExecutor;
import com.graphaware.tx.executor.single.VoidReturningCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Map;

import static com.graphaware.common.description.predicate.Predicates.any;
import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.wildcard;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Integration test for {@link com.graphaware.module.relcount.cache.DegreeCachingNode}.
 */
public abstract class DegreeCachingNodeIntegrationTest {

    protected GraphDatabaseService database;
    private TransactionExecutor txExecutor;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            database.createNode(); //ID = 0
            tx.success();
        }

        setUpModuleConfig();

        txExecutor = new SimpleTransactionExecutor(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    protected abstract DegreeCachingNode cachingNode();

    protected RelationshipCounter counter() {
        return new CachedRelationshipCounter(database);
    }

    @Test
    public void correctNodeIdShouldBeReturned() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0L, cachingNode().getId());
        }
    }

    @Test
    public void shouldCorrectlyReturnAllCachedCounts() {
        setUpRelationshipCounts();

        Map<DetachedRelationshipDescription, Integer> relationshipCounts;
        try (Transaction tx = database.beginTx()) {
            relationshipCounts = cachingNode().getCachedDegrees();
        }

        assertEquals(3, (int) relationshipCounts.get(literal("test", OUTGOING).with("key1", equalTo("value1")).with("key2", equalTo("value2"))));
        assertEquals(11, (int) relationshipCounts.get(literal("test", OUTGOING).with("key1", equalTo("value2"))));
        assertEquals(7, (int) relationshipCounts.get(literal("test", OUTGOING).with("key1", equalTo("value1"))));
        assertEquals(13, (int) relationshipCounts.get(literal("test", OUTGOING)));
        assertEquals(5, (int) relationshipCounts.get(literal("test", OUTGOING).with("key1", equalTo("value1")).with("key2", equalTo("value3"))));
        assertEquals(20, (int) relationshipCounts.get(literal("test2", OUTGOING).with("key3", any())));

        assertEquals(6, relationshipCounts.size());
    }

    @Test
    public void incrementingCountOnNonExistingCachedRelationshipShouldMakeItOne() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value3")), 1);
                node.flush();
            }
        });

        Map<DetachedRelationshipDescription, Integer> relationshipCounts;
        try (Transaction tx = database.beginTx()) {
            relationshipCounts = cachingNode().getCachedDegrees();
        }

        assertEquals(1, (int) relationshipCounts.get(literal("test", OUTGOING).with("key1", equalTo("value3"))));
    }

    @Test
    public void incrementingCountByFiveOnNonExistingCachedRelationshipShouldMakeItFive() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value3")), 5);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(5, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value3"))));
        }
    }

    @Test
    public void incrementingCountOnExistingCachedRelationshipShouldMakeItPlusOne() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value2")), 1);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(4, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value2"))));
        }
    }

    @Test
    public void incrementingCountByFiveOnExistingCachedRelationshipShouldMakeItPlusFive() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")), 5);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(7, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value1"))));
        }
    }

    @Test
    public void incrementingShouldNotAffectMoreGeneralRelationships() {
        setUpRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")), 5);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(12, (int) cachingNode().getCachedDegrees().get(literal("test", OUTGOING).with("key1", equalTo("value1"))));
            assertEquals(13, (int) cachingNode().getCachedDegrees().get(literal("test", OUTGOING)));
        }
    }

    @Test
    public void decrementingCountOnNonExistingCachedRelationshipShouldThrowException() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                try {
                    cachingNode().decrementDegree(literal("test", OUTGOING).with("key1", equalTo("value3")), 1);
                    fail();
                } catch (NeedsInitializationException e) {
                    //ok
                }
            }
        });
    }

    @Test
    public void decrementingCountByTwoOnNonExistingCachedRelationshipShouldThrowException() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                try {
                    cachingNode().decrementDegree(literal("test", OUTGOING).with("key1", equalTo("value3")), 2);
                    fail();
                } catch (NeedsInitializationException e) {
                    //OK
                }
            }
        });
    }

    @Test
    public void decrementingCountOnExistingCachedRelationshipShouldMakeItMinusOne() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.decrementDegree(literal("test", OUTGOING).with("key1", equalTo("value2")), 1);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value2"))));
        }
    }

    @Test
    public void decrementingCountByTwoOnExistingCachedRelationshipShouldMakeItMinusTwo() {
        setUpBasicRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.decrementDegree(literal("test", OUTGOING).with("key2", equalTo("value2")), 2);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key2", equalTo("value2"))));
        }
    }

    @Test
    public void decrementingShouldNotAffectMoreGeneralRelationships() {
        setUpRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.decrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")), 5);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, (int) cachingNode().getCachedDegrees().get(literal("test", OUTGOING).with("key1", equalTo("value1"))));
            assertEquals(13, (int) cachingNode().getCachedDegrees().get(literal("test", OUTGOING)));
        }
    }

    @Test
    public void decrementingCountByTooMuchShouldBeIndicated() {
        setUpRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                try {
                    cachingNode().decrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")), 8);
                    fail();
                } catch (NeedsInitializationException e) {
                    //OK
                }
            }
        });
    }

    @Test
    public void decrementingCountByToZeroShouldDeleteCachedCount() {
        setUpRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.decrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")), 7);
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertFalse(cachingNode().getCachedDegrees().containsKey(literal("test", OUTGOING).with("key1", equalTo("value1"))));
            assertEquals(8, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value1"))));
            assertEquals(13, (int) cachingNode().getCachedDegrees().get(literal("test", OUTGOING)));

            tx.success();
        }
    }

    @Test
    public void shouldProperlyDeleteCounts() {
        setUpRelationshipCounts();

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.decrementDegree(literal("test", OUTGOING), node.getCachedDegrees().get(literal("test", OUTGOING)));
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertEquals(26, counter().count(database.getNodeById(0), wildcard("test", OUTGOING)));
            assertFalse(cachingNode().getCachedDegrees().containsKey(literal("test", OUTGOING)));

            assertEquals(11, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value2"))));
            assertEquals(15, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value1"))));
            assertEquals(3, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value1")).with("key2", equalTo("value2"))));

            tx.success();
        }

        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode node = cachingNode();
                node.decrementDegree(literal("test", OUTGOING).with("key1", equalTo("value2")), node.getCachedDegrees().get(literal("test", OUTGOING).with("key1", equalTo("value2"))));
                node.flush();
            }
        });

        try (Transaction tx = database.beginTx()) {
            assertFalse(cachingNode().getCachedDegrees().containsKey(literal("test", OUTGOING)));
            assertFalse(cachingNode().getCachedDegrees().containsKey(literal("test", OUTGOING).with("key1", equalTo("value2"))));

            assertEquals(15, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value1"))));
            assertEquals(3, counter().count(database.getNodeById(0), wildcard("test", OUTGOING).with("key1", equalTo("value1")).with("key2", equalTo("value2"))));

            tx.success();
        }
    }

    private void setUpBasicRelationshipCounts() {
        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode degreeCachingNode = cachingNode();
                degreeCachingNode.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")), 2);
                degreeCachingNode.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value2")), 3);
                degreeCachingNode.incrementDegree(literal("test", OUTGOING).with("key2", equalTo("value2")), 4);
                degreeCachingNode.flush();
            }
        });
    }

    private void setUpRelationshipCounts() {
        txExecutor.executeInTransaction(new VoidReturningCallback() {
            @Override
            public void doInTx(GraphDatabaseService database) {
                DegreeCachingNode degreeCachingNode = cachingNode();
                degreeCachingNode.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")).with("key2", equalTo("value2")), 3);
                degreeCachingNode.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")).with("key2", equalTo("value3")), 5);
                degreeCachingNode.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value1")), 7);
                degreeCachingNode.incrementDegree(literal("test", OUTGOING).with("key1", equalTo("value2")), 11);
                degreeCachingNode.incrementDegree(literal("test", OUTGOING), 13);
                degreeCachingNode.incrementDegree(literal("test2", OUTGOING).with("key3", any()), 20);
                degreeCachingNode.flush();
            }
        });
    }

    private void setUpModuleConfig() {
        try (Transaction tx = database.beginTx()) {
            Node root = ProductionGraphAwareRuntime.getOrCreateRoot(database);
            String key = DefaultRuntimeConfiguration.getInstance().createPrefix(BaseGraphAwareRuntime.RUNTIME) + RelationshipCountRuntimeModule.FULL_RELCOUNT_DEFAULT_ID;
            root.setProperty(key, Serializer.toString(getConfiguration(), BaseGraphAwareRuntime.CONFIG));
            tx.success();
        }
    }

    protected abstract RelationshipCountConfiguration getConfiguration();
}
