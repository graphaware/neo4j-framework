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

package com.graphaware.tx.event.improved;

import com.graphaware.common.policy.inclusion.*;
import com.graphaware.common.policy.inclusion.fluent.IncludeNodes;
import com.graphaware.common.util.Change;
import com.graphaware.test.util.TestDataBuilder;
import com.graphaware.tx.event.improved.api.FilteredTransactionData;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import com.graphaware.tx.executor.single.*;
import org.junit.After;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.graphaware.common.util.Change.changesToMap;
import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.common.util.IterableUtils.countNodes;
import static com.graphaware.common.util.PropertyContainerUtils.*;
import static com.graphaware.tx.event.improved.LazyTransactionDataComprehensiveTest.*;
import static com.graphaware.tx.event.improved.PropertiesAssert.assertProperties;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.graphdb.RelationshipType.withName;
import static org.neo4j.kernel.configuration.Settings.*;

/**
 * Integration test for {@link com.graphaware.tx.event.improved.api.FilteredTransactionData}.
 */
public class FilteredLazyTransactionDataIntegrationTest {

    private static final String INTERNAL_PREFIX = "_GA_";
    private static final String INTERNAL_NODE_PROPERTY = INTERNAL_PREFIX + "LABEL";

    private GraphDatabaseService database;

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void allInternalMutationsShouldLookLikeNoMutations() {
        createTestDatabaseForInternalTest();
        mutateGraph(new InternalTestGraphMutation(), new BeforeCommitCallback() {
            @Override
            public void doBeforeCommit(ImprovedTransactionData transactionData) {
                assertFalse(transactionData.mutationsOccurred());
            }
        });
    }

    @Test
    public void changeOfPropertyToNonInternalOnRelationshipShouldBePickedUp() {
        createTestDatabaseForInternalTest();

        mutateGraph(new InternalTestGraphMutation(), new BeforeCommitCallback() {
            @Override
            public void doBeforeCommit(ImprovedTransactionData transactionData) {
                //nothing here
            }
        });

        final AtomicBoolean mutationsOccurred = new AtomicBoolean(false);

        mutateGraph(new VoidReturningCallback() {
                        @Override
                        protected void doInTx(GraphDatabaseService database) {
                            Node three = database.getNodeById(3);
                            Relationship r = three.getSingleRelationship(withName("R4"), OUTGOING);
                            r.setProperty("non-internal", 4); //this makes it non-internal
                        }
                    }, new BeforeCommitCallback() {
                        @Override
                        public void doBeforeCommit(ImprovedTransactionData transactionData) {
                            if (transactionData.mutationsOccurred()) {
                                mutationsOccurred.set(true);
                            }
                        }
                    }
        );

        assertTrue(mutationsOccurred.get());
    }

    @Test //bug test
    public void changeOfLabelShouldBePickedUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(label("Person"));
            node.setProperty("name", "Michal");
            tx.success();
        }

        final AtomicBoolean mutationsOccurred = new AtomicBoolean(false);

        mutateGraph(new LabelChange(), new BeforeCommitCallback() {
            @Override
            public void doBeforeCommit(ImprovedTransactionData transactionData) {
                if (transactionData.mutationsOccurred()) {
                    mutationsOccurred.set(true);
                }
            }
        });

        assertTrue(mutationsOccurred.get());
    }

    @Test
    public void removedLabelShouldBePickedUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node = database.createNode(label("Person"));
            node.setProperty("name", "Michal");
            tx.success();
        }

        final AtomicBoolean mutationsOccurred = new AtomicBoolean(false);
        database.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Object>() {
            @Override
            public Object beforeCommit(TransactionData data) throws Exception {
                mutationsOccurred.set(new FilteredTransactionData(new LazyTransactionData(data), InclusionPolicies.all().with(IncludeNodes.all().with(label("Person")))).mutationsOccurred());
                return null;
            }
        });


        try (Transaction tx = database.beginTx()) {
            database.findNodes(label("Person")).next().removeLabel(label("Person"));
            tx.success();
        }

        assertTrue(mutationsOccurred.get());
    }

    @Test
    public void policiesThatIgnoreEverythingShouldBeHonoured() {
        createTestDatabase();

        database.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Void>() {
            @Override
            public Void beforeCommit(TransactionData data) throws Exception {
                ImprovedTransactionData improvedTransactionData = new FilteredTransactionData(new LazyTransactionData(data), InclusionPolicies.none());
                assertFalse(improvedTransactionData.mutationsOccurred());
                assertTrue(improvedTransactionData.hasBeenChanged(database.getNodeById(1)));
                assertTrue(improvedTransactionData.changedProperties(database.getNodeById(1)).isEmpty());
                return null;
            }
        });
        new SimpleTransactionExecutor(database).executeInTransaction(new TestGraphMutation());

    }

    @Test
    public void createdRelationshipsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> created = propertyContainersToMap(transactionData.getAllCreatedRelationships());
                        assertEquals(2, created.size());

                        long r1Id = database.getNodeById(5).getSingleRelationship(withName("R2"), OUTGOING).getId();
                        Relationship r1 = created.get(r1Id);
                        assertProperties(r1);

                        long r2Id = database.getNodeById(1).getSingleRelationship(withName("R1"), OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);
                        assertProperties(r2);

                        assertTrue(transactionData.hasBeenCreated(r1));
                        assertTrue(transactionData.hasBeenCreated(r2));
                        assertFalse(transactionData.hasBeenCreated(database.getNodeById(3).getSingleRelationship(withName("R1"), OUTGOING)));

                        assertFalse(r2.getEndNode().hasProperty("place"));  //filtered out
                        assertEquals(4, r2.getEndNode().getSingleRelationship(withName("R1"), OUTGOING).getEndNode().getId()); //node filtered out but when accessed as a relationship end node it is present
                        assertNull(r2.getEndNode().getSingleRelationship(withName("R3"), OUTGOING)); //filtered out
                    }
                }
        );
    }

    @Test
    public void startingWithCreatedRelationshipCurrentGraphVersionShouldBeTraversed() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> created = propertyContainersToMap(transactionData.getAllCreatedRelationships());

                        long r2Id = database.getNodeById(1).getSingleRelationship(withName("R1"), OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);
                        assertProperties(r2.getStartNode(), NAME, "NewOne", TAGS, new String[]{"one", "three"});

                        assertNull(r2.getEndNode().getSingleRelationship(withName("R3"), INCOMING));
                        assertEquals(5, r2.getEndNode().getSingleRelationship(withName("R2"), INCOMING).getStartNode().getId());
                    }
                }
        );
    }

    @Test
    public void changedRelationshipsShouldBeCorrectlyIdentified() {
        createTestDatabase();

        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        assertEquals(0, changed.size()); //R3 filtered out
                        assertTrue(transactionData.hasBeenChanged(database.getRelationshipById(changedRelId.get())));
                        assertFalse(transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getCurrent().hasProperty("time")); //filtered
                        assertTrue(transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getCurrent().hasProperty("tags"));
                        assertFalse(transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getPrevious().hasProperty("time")); //filtered
                        assertFalse(transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getPrevious().hasProperty("tags"));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedRelationshipPreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();

        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Relationship previous = transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getPrevious();
                        assertProperties(previous.getEndNode(), NAME, "One", COUNT, 1, TAGS, new String[]{"one", "two"});

                        assertEquals("Three", previous.getStartNode().getProperty("name"));
                        assertEquals("nothing", previous.getStartNode().getProperty("tags", "nothing"));

                        Node endNode = previous.getEndNode();
                        Relationship r1 = endNode.getSingleRelationship(withName("R1"), OUTGOING);
                        Node endNode1 = r1.getEndNode();
                        assertEquals("Two", endNode1.getProperty("name"));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedRelationshipPreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();

        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Relationship previous = transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getPrevious();

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
                                .relationships(withName("R3"), INCOMING)
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL)
                                .evaluator(Evaluators.toDepth(3));

                        assertEquals(4, count(traversalDescription.traverse(previous.getEndNode()).nodes()));
                    }
                }
        );
    }

    @Test
    public void startingWithCurrentChangedRelationshipCurrentGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();

        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Relationship current = transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getCurrent();

                        current = transactionData.getChanged(current).getCurrent();
                        assertProperties(current.getEndNode(), NAME, "NewOne", TAGS, new String[]{"one", "three"});

                        assertEquals("Three", current.getStartNode().getProperty("name"));
                        assertEquals("none", current.getStartNode().getProperty("place", "none")); //filtered
                        assertEquals("one", current.getStartNode().getProperty("tags"));

                        assertEquals("Three", current.getEndNode().getSingleRelationship(withName("R1"), OUTGOING).getEndNode().getProperty("name"));
                    }
                }
        );
    }

    @Test
    public void startingWithCurrentChangedRelationshipCurrentGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();

        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Relationship current = transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getCurrent();

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
                                .relationships(withName("R3"), INCOMING)
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL);

                        assertEquals(3, count(traversalDescription.traverse(current.getEndNode()).nodes()));
                    }
                }
        );
    }

    @Test
    public void deletedRelationshipsShouldBeCorrectlyIdentified() {
        createTestDatabase();

        final Holder<Relationship> deletedRelationship = new Holder<>();
        final Holder<Node> deletedNode = new Holder<>();
        try (Transaction tx = database.beginTx()) {
            deletedRelationship.set(database.getNodeById(1).getSingleRelationship(withName("R3"), OUTGOING));
            deletedNode.set(database.getNodeById(2));
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> deleted = propertyContainersToMap(transactionData.getAllDeletedRelationships());
                        assertEquals(3, deleted.size());

                        long r1Id = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getSingleRelationship(withName("R1"), INCOMING).getId();
                        Relationship r1 = deleted.get(r1Id);
                        try {
                            r1.getProperty("time");
                            fail();
                        } catch (NotFoundException e) {
                            //ok
                        }
                        assertProperties(r1);

                        long r2Id = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getSingleRelationship(withName("R2"), INCOMING).getId();
                        Relationship r2 = deleted.get(r2Id);
                        assertProperties(r2);

                        Iterator<Relationship> relationships = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getRelationships(withName("R2"), OUTGOING).iterator();
                        long r3Id = relationships.next().getId();
                        if (r3Id == r2Id) {
                            r3Id = relationships.next().getId();
                        }
                        Relationship r3 = deleted.get(r3Id);
                        assertEquals("nothing", r3.getProperty("time", "nothing"));
                        assertProperties(r3);

                        Relationship r4 = transactionData.getDeleted(deletedRelationship.get());
                        assertProperties(r4);

                        assertTrue(transactionData.hasBeenDeleted(r1));
                        assertTrue(transactionData.hasBeenDeleted(r2));
                        assertTrue(transactionData.hasBeenDeleted(r3));
                        assertTrue(transactionData.hasBeenDeleted(r4));
                        assertFalse(transactionData.hasBeenDeleted(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING)));

                        assertEquals(3, count(transactionData.getDeletedRelationships(deletedNode.get())));
                        assertEquals(3, count(transactionData.getDeletedRelationships(deletedNode.get(), withName("R2"), withName("R1"))));
                        assertEquals(2, count(transactionData.getDeletedRelationships(deletedNode.get(), withName("R2"))));
                        assertEquals(2, count(transactionData.getDeletedRelationships(deletedNode.get(), OUTGOING)));
                        assertEquals(2, count(transactionData.getDeletedRelationships(deletedNode.get(), OUTGOING, withName("R2"))));
                        assertEquals(1, count(transactionData.getDeletedRelationships(deletedNode.get(), INCOMING, withName("R2"))));
                        assertEquals(0, count(transactionData.getDeletedRelationships(deletedNode.get(), withName("R3"))));
                    }
                }
        );
    }

    @Test
    public void startingWithDeletedRelationshipPreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();

        final Holder<Relationship> deletedRel = new Holder<>();
        try (Transaction tx = database.beginTx()) {
            deletedRel.set(database.getNodeById(1).getSingleRelationship(withName("R3"), OUTGOING));
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Relationship r4 = transactionData.getDeleted(deletedRel.get());

                        Relationship deletedRel = transactionData.getDeleted(r4);
                        assertProperties(deletedRel.getStartNode(), NAME, "One", COUNT, 1, TAGS, new String[]{"one", "two"});

                        assertEquals("Three", deletedRel.getEndNode().getProperty("name"));
                        assertEquals("nothing", deletedRel.getEndNode().getProperty("tags", "nothing"));

                        Node startNode = deletedRel.getStartNode();
                        Relationship r5 = startNode.getSingleRelationship(withName("R1"), OUTGOING);
                        assertEquals("Two", r5.getEndNode().getProperty("name"));

                        assertEquals(2, count(startNode.getRelationships()));
                        assertEquals(2, count(startNode.getRelationships(OUTGOING)));
                        assertEquals(0, count(startNode.getRelationships(withName("R3"))));
                        assertEquals(1, count(startNode.getRelationships(withName("R3"), withName("R1"))));
                        assertEquals(0, count(startNode.getRelationships(INCOMING, withName("R3"), withName("R1"))));
                    }
                }
        );
    }

    @Test
    public void startingWithDeletedRelationshipPreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();

        final Holder<Relationship> deletedRelationship = new Holder<>();
        try (Transaction tx = database.beginTx()) {
            deletedRelationship.set(database.getNodeById(1).getSingleRelationship(withName("R3"), OUTGOING));
        }

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Relationship deletedRel = transactionData.getDeleted(deletedRelationship.get());

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
                                .relationships(withName("R3"), INCOMING)
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL)
                                .evaluator(Evaluators.toDepth(3));

                        assertEquals(4, count(traversalDescription.traverse(deletedRel.getStartNode()).nodes()));
                    }
                }
        );
    }

    @Test
    public void createdRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getChanged(database.getRelationshipById(changedRelId.get()));

                        assertTrue(transactionData.hasPropertyBeenCreated(change.getCurrent(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenCreated(change.getCurrent(), "time"));
                        assertTrue(transactionData.hasPropertyBeenCreated(change.getPrevious(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenCreated(change.getPrevious(), "tag"));

                        assertEquals(1, transactionData.createdProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.createdProperties(change.getPrevious()).size());
                        assertEquals("cool", transactionData.createdProperties(change.getCurrent()).get("tags"));
                        assertEquals("cool", transactionData.createdProperties(change.getPrevious()).get("tags"));

                        assertFalse(transactionData.hasPropertyBeenCreated(transactionData.getAllDeletedRelationships().iterator().next(), "tags"));

                        //created relationship should not fall into this category
                        assertFalse(transactionData.hasPropertyBeenCreated(database.getNodeById(5).getSingleRelationship(withName("R2"), OUTGOING), "time"));
                    }
                }
        );
    }

    @Test
    public void changedRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getChanged(database.getRelationshipById(changedRelId.get()));

                        assertTrue(transactionData.hasPropertyBeenChanged(change.getCurrent(), "time"));
                        assertFalse(transactionData.hasPropertyBeenChanged(change.getCurrent(), "tags"));
                        assertTrue(transactionData.hasPropertyBeenChanged(change.getPrevious(), "time"));
                        assertFalse(transactionData.hasPropertyBeenChanged(change.getPrevious(), "tag"));

                        assertEquals(0, transactionData.changedProperties(change.getCurrent()).size()); //filtered
                        assertEquals(0, transactionData.changedProperties(change.getPrevious()).size()); //filtered
                    }
                }
        );
    }

    @Test
    public void deletedRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getChanged(database.getRelationshipById(changedRelId.get()));

                        assertTrue(transactionData.hasPropertyBeenDeleted(change.getCurrent(), "tag"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(change.getCurrent(), "time"));
                        assertTrue(transactionData.hasPropertyBeenDeleted(change.getPrevious(), "tag"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(change.getPrevious(), "tags"));

                        assertEquals(1, transactionData.deletedProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.deletedProperties(change.getPrevious()).size());
                        assertEquals("cool", transactionData.deletedProperties(change.getCurrent()).get("tag"));
                        assertEquals("cool", transactionData.deletedProperties(change.getPrevious()).get("tag"));

                        //deleted relationships' props don't qualify
                        Iterator<Relationship> iterator = transactionData.getAllDeletedRelationships().iterator();
                        assertFalse(transactionData.hasPropertyBeenDeleted(iterator.next(), "time"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(iterator.next(), "time"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(iterator.next(), "time"));
                    }
                }
        );
    }

    @Test
    public void createdNodesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> createdNodes = propertyContainersToMap(transactionData.getAllCreatedNodes());
                        assertEquals(1, createdNodes.size());

                        Node createdNode = createdNodes.get(5L);
                        assertProperties(createdNode, NAME, "Five", "size", 4L);

                        assertTrue(transactionData.hasBeenCreated(createdNode));
                        assertFalse(transactionData.hasBeenCreated(database.getNodeById(3)));
                    }
                }
        );
    }

    @Test
    public void startingWithCreatedNodeCurrentGraphVersionShouldBeTraversed() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> createdNodes = propertyContainersToMap(transactionData.getAllCreatedNodes());
                        Node createdNode = createdNodes.get(5L);

                        assertEquals("one", createdNode.getSingleRelationship(withName("R2"), OUTGOING).getEndNode().getProperty("tags"));
                        assertEquals("not there", createdNode.getSingleRelationship(withName("R2"), OUTGOING).getEndNode().getProperty("place", "not there"));
                        assertFalse(createdNode.getSingleRelationship(withName("R2"), OUTGOING).getEndNode().getRelationships(withName("R3"), INCOMING).iterator().hasNext());
                    }
                }
        );
    }

    @Test
    public void changedNodesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());
                        assertEquals(2, changed.size());

                        Node previous1 = changed.get(1L).getPrevious();
                        assertProperties(previous1, NAME, "One", COUNT, 1, TAGS, new String[]{"one", "two"});

                        Node current1 = changed.get(1L).getCurrent();
                        assertProperties(current1, NAME, "NewOne", TAGS, new String[]{"one", "three"});

                        Node previous2 = changed.get(3L).getPrevious();
                        assertProperties(previous2, NAME, "Three");

                        Node current2 = changed.get(3L).getCurrent();
                        assertProperties(current2, NAME, "Three", TAGS, "one");

                        assertTrue(transactionData.hasBeenChanged(previous1));
                        assertTrue(transactionData.hasBeenChanged(previous2));
                        assertTrue(transactionData.hasBeenChanged(current1));
                        assertTrue(transactionData.hasBeenChanged(current2));
                        assertFalse(transactionData.hasBeenChanged(database.getNodeById(4)));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedNodePreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node current = changed.get(1L).getCurrent();
                        Node previous = transactionData.getChanged(current).getPrevious();

                        assertFalse(previous.getSingleRelationship(withName("R1"), OUTGOING).hasProperty("time"));
                        assertEquals("Two", previous.getRelationships(withName("R1"), OUTGOING).iterator().next().getEndNode().getProperty("name"));
                        assertNull(previous.getSingleRelationship(withName("R3"), OUTGOING));
                        assertEquals(2L, previous.getRelationships(withName("R1")).iterator().next().getEndNode().getProperty("size"));

                        assertNull(previous.getSingleRelationship(withName("R1"), INCOMING));
                        assertEquals(2, count(previous.getRelationships()));
                        assertEquals(1, count(previous.getRelationships(withName("R1"), withName("R3"))));
                        assertEquals(1, count(previous.getRelationships(withName("R1"))));
                        assertEquals(2, count(previous.getRelationships(OUTGOING)));
                        assertEquals(0, count(previous.getRelationships(INCOMING)));
                        assertEquals(0, count(previous.getRelationships(OUTGOING, withName("R3"))));
                        assertEquals(1, count(previous.getRelationships(OUTGOING, withName("R1"), withName("R3"))));
                        assertEquals(0, count(previous.getRelationships(withName("R3"), OUTGOING)));

                        assertTrue(previous.hasRelationship());
                        assertTrue(previous.hasRelationship(withName("R1"), withName("R3")));
                        assertTrue(previous.hasRelationship(withName("R1")));
                        assertTrue(previous.hasRelationship(OUTGOING));
                        assertFalse(previous.hasRelationship(INCOMING));
                        assertFalse(previous.hasRelationship(OUTGOING, withName("R3")));
                        assertTrue(previous.hasRelationship(OUTGOING, withName("R1"), withName("R3")));
                        assertFalse(previous.hasRelationship(withName("R3"), OUTGOING));
                        assertFalse(previous.hasRelationship(withName("R1"), INCOMING));
                        assertFalse(previous.hasRelationship(withName("R2")));

                        previous.createRelationshipTo(database.getNodeById(4), withName("R3"));
                        assertNull(previous.getSingleRelationship(withName("R3"), OUTGOING));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedNodePreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node previous = changed.get(1L).getPrevious();

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
                                .relationships(withName("R3"), INCOMING)
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL)
                                .evaluator(Evaluators.toDepth(3));

                        assertEquals(4, count(traversalDescription.traverse(previous).nodes()));
                    }
                }
        );
    }

    @Test
    public void startingWithCurrentChangedNodeCurrentGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());
                        Node current = changed.get(1L).getCurrent();

                        assertEquals("Three", current.getSingleRelationship(withName("R1"), OUTGOING).getEndNode().getProperty("name"));
                        assertEquals("not there", current.getRelationships(withName("R1"), OUTGOING).iterator().next().getEndNode().getProperty("place", "not there"));
                        assertEquals("one", current.getRelationships(OUTGOING, withName("R1")).iterator().next().getEndNode().getProperty("tags"));
                    }
                }
        );
    }

    @Test
    public void startingWithCurrentChangedNodeCurrentGraphVersionShouldBeTraversed() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());
                        Node current = changed.get(1L).getCurrent();

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
                                .relationships(withName("R3"), INCOMING)
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL);

                        assertEquals(3, count(traversalDescription.traverse(current).nodes()));

                    }
                }
        );
    }

    @Test
    public void deletedNodesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        assertEquals(1, deletedNodes.size());

                        Node deleted = deletedNodes.get(2L);
                        Node deleted1 = transactionData.getDeleted(deleted);
                        Node one = deleted1.getSingleRelationship(withName("R1"), INCOMING).getStartNode();

                        assertProperties(deleted, NAME, "Two", "size", 2L);

                        assertTrue(transactionData.hasBeenDeleted(deleted));
                        assertFalse(transactionData.hasBeenDeleted(one));
                    }
                }
        );
    }


    @Test
    public void startingWithDeletedNodePreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);

                        Node one = transactionData.getDeleted(deleted).getSingleRelationship(withName("R1"), INCOMING).getStartNode();

                        assertEquals("bla", one.getSingleRelationship(withName("R1"), OUTGOING).getProperty("time", "bla"));
                        assertEquals("Two", one.getRelationships(withName("R1"), OUTGOING).iterator().next().getEndNode().getProperty("name"));
                        assertNull(one.getSingleRelationship(withName("R3"), OUTGOING));
                        assertEquals(2L, one.getRelationships(withName("R1")).iterator().next().getEndNode().getProperty("size"));
                    }
                }
        );
    }

    @Test
    public void startingWithDeletedNodePreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);

                        Node one = transactionData.getDeleted(deleted).getSingleRelationship(withName("R1"), INCOMING).getStartNode();

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
                                .relationships(withName("R3"), INCOMING)
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL)
                                .evaluator(Evaluators.toDepth(3));

                        assertEquals(4, count(traversalDescription.traverse(one).nodes()));
                    }
                }
        );
    }

    @Test
    public void createdNodePropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Node> changed = changesToMap(transactionData.getAllChangedNodes()).get(3L);

                        assertTrue(transactionData.hasPropertyBeenCreated(changed.getCurrent(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenCreated(changed.getCurrent(), "name"));
                        assertTrue(transactionData.hasPropertyBeenCreated(changed.getPrevious(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenCreated(changed.getPrevious(), "name"));

                        assertEquals(1, transactionData.createdProperties(changed.getCurrent()).size());
                        assertEquals(1, transactionData.createdProperties(changed.getPrevious()).size());
                        assertEquals("one", transactionData.createdProperties(changed.getCurrent()).get("tags"));
                        assertEquals("one", transactionData.createdProperties(changed.getPrevious()).get("tags"));

                        assertFalse(transactionData.hasPropertyBeenCreated(changesToMap(transactionData.getAllChangedNodes()).get(1L).getCurrent(), "tags"));
                    }
                }
        );
    }

    @Test
    public void changedNodePropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Node> changed = changesToMap(transactionData.getAllChangedNodes()).get(1L);

                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getCurrent(), "name"));
                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getCurrent(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), "count"));
                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getPrevious(), "name"));
                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getPrevious(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), "count"));

                        assertEquals(2, transactionData.changedProperties(changed.getCurrent()).size());
                        assertEquals(2, transactionData.changedProperties(changed.getPrevious()).size());
                        assertEquals("One", transactionData.changedProperties(changed.getCurrent()).get("name").getPrevious());
                        assertEquals("One", transactionData.changedProperties(changed.getPrevious()).get("name").getPrevious());
                        assertEquals("NewOne", transactionData.changedProperties(changed.getCurrent()).get("name").getCurrent());
                        assertEquals("NewOne", transactionData.changedProperties(changed.getPrevious()).get("name").getCurrent());
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) transactionData.changedProperties(changed.getCurrent()).get("tags").getCurrent()));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) transactionData.changedProperties(changed.getPrevious()).get("tags").getPrevious()));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) transactionData.changedProperties(changed.getCurrent()).get("tags").getCurrent()));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) transactionData.changedProperties(changed.getPrevious()).get("tags").getPrevious()));

                        assertEquals(3, count(changed.getPrevious().getPropertyKeys()));
                        assertEquals(2, count(changed.getCurrent().getPropertyKeys()));

                        changed = changesToMap(transactionData.getAllChangedNodes()).get(3L);
                        assertEquals(0, transactionData.changedProperties(changed.getCurrent()).size());
                        assertEquals(0, transactionData.changedProperties(changed.getPrevious()).size());
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), "name"));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), "place"));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), "name"));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), "place"));

                        assertFalse(transactionData.hasPropertyBeenChanged(transactionData.getAllDeletedNodes().iterator().next(), "name"));
                        assertFalse(transactionData.hasPropertyBeenChanged(transactionData.getAllCreatedNodes().iterator().next(), "name"));

                        assertEquals(1, count(changed.getPrevious().getPropertyKeys()));
                        assertEquals(2, count(changed.getCurrent().getPropertyKeys()));

                        //one that isn't changed
                        Node unchanged = changesToMap(transactionData.getAllChangedNodes()).get(1L).getPrevious().getSingleRelationship(withName("WHATEVER"), OUTGOING).getEndNode();
                        assertEquals(1, count(unchanged.getPropertyKeys()));
                        assertEquals("name", unchanged.getPropertyKeys().iterator().next());
                        assertEquals("Four", unchanged.getProperty("name"));
                        assertEquals("Four", unchanged.getProperty("name", "nothing"));
                        assertEquals("nothing", unchanged.getProperty("non-existing", "nothing"));

                    }
                }
        );
    }

    @Test
    public void deletedNodePropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Node> changed = changesToMap(transactionData.getAllChangedNodes()).get(1L);

                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), "name"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), "tags"));
                        assertTrue(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), "count"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), "name"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), "tags"));
                        assertTrue(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), "count"));

                        assertEquals(1, transactionData.deletedProperties(changed.getCurrent()).size());
                        assertEquals(1, transactionData.deletedProperties(changed.getPrevious()).size());
                        assertEquals(1, transactionData.deletedProperties(changed.getCurrent()).get("count"));
                        assertEquals(1, transactionData.deletedProperties(changed.getPrevious()).get("count"));

                        changed = changesToMap(transactionData.getAllChangedNodes()).get(3L);
                        assertEquals(0, transactionData.deletedProperties(changed.getCurrent()).size());
                        assertEquals(0, transactionData.deletedProperties(changed.getPrevious()).size());
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), "name"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), "place"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), "name"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), "place"));

                        assertFalse(transactionData.hasPropertyBeenDeleted(transactionData.getAllDeletedNodes().iterator().next(), "name"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(transactionData.getAllCreatedNodes().iterator().next(), "name"));
                    }
                }
        );
    }

    //mutations

    @Test
    public void shouldBeAbleToChangeCreatedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Relationship> created = propertyContainersToMap(transactionData.getAllCreatedRelationships());

                        long r1Id = database.getNodeById(5).getSingleRelationship(withName("R2"), OUTGOING).getId();
                        Relationship r1 = created.get(r1Id);

                        r1.setProperty("additional", "someValue");
                        r1.removeProperty("time");
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {
            Relationship r1 = database.getNodeById(5).getSingleRelationship(withName("R2"), OUTGOING);
            assertEquals(1, count(r1.getPropertyKeys()));
            assertEquals("someValue", r1.getProperty("additional"));
            assertFalse(r1.hasProperty("time"));
        }
    }

    @Test
    public void shouldBeAbleToChangeCreatedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Node> createdNodes = propertyContainersToMap(transactionData.getAllCreatedNodes());
                        Node createdNode = createdNodes.get(5L);

                        createdNode.setProperty("name", "NewFive");
                        createdNode.setProperty("additional", "something");
                        createdNode.removeProperty("size");
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {

            Node createdNode = database.getNodeById(5L);
            assertProperties(createdNode, NAME, "NewFive", "additional", "something");
            assertFalse(createdNode.hasProperty("size"));
        }
    }

    @Test
    public void shouldBeAbleToChangeCurrentChangedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        node.setProperty("name", "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty("tags");
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {

            Node node = database.getNodeById(1L);
            assertProperties(node, NAME, "YetAnotherOne", "additional", "something");
            assertFalse(node.hasProperty("tags"));
        }
    }

    @Test
    public void shouldBeAbleToChangePreviousChangedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        node.setProperty("name", "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty("tags");
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {

            Node node = database.getNodeById(1L);

            assertEquals(2, count(node.getPropertyKeys()));
            assertEquals("YetAnotherOne", node.getProperty("name"));
            assertEquals("something", node.getProperty("additional"));
            assertFalse(node.hasProperty("tags"));
        }
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToChangeDeletedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Relationship> deleted = propertyContainersToMap(transactionData.getAllDeletedRelationships());

                        long r1Id = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getSingleRelationship(withName("R1"), INCOMING).getId();
                        Relationship r1 = deleted.get(r1Id);

                        try {
                            r1.setProperty("irrelevant", "irrelevant");
                            return;
                        } catch (IllegalStateException e) {
                            //OK
                        }

                        r1.removeProperty("irrelevant");
                    }
                }
        );
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToChangeDeletedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());

                        Node deleted = deletedNodes.get(2L);

                        try {
                            deleted.setProperty("irrelevant", "irrelevant");
                            return;
                        } catch (IllegalStateException e) {
                            //OK
                        }

                        deleted.removeProperty("irrelevant");
                    }
                }
        );
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToCreateARelationshipFromDeletedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());

                        Node deleted = deletedNodes.get(2L);

                        deleted.createRelationshipTo(database.getNodeById(3), withName("illegal"));
                    }
                }
        );
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToCreateARelationshipToDeletedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());

                        Node deleted = deletedNodes.get(2L);

                        database.getNodeById(3).createRelationshipTo(deleted, withName("illegal"));
                    }
                }
        );
    }

    @Test
    public void shouldChangeNothingIfTxRollsBack() {
        createTestDatabase();
        mutateGraph(
                new TestGraphMutation(),
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        throw new RuntimeException("Deliberate testing exception");
                    }
                },
                KeepCalmAndCarryOn.getInstance()
        );

        try (Transaction tx = database.beginTx()) {

            long r4Id = database.getNodeById(3L).getSingleRelationship(withName("R3"), INCOMING).getId();
            Relationship r4 = database.getRelationshipById(r4Id);

            assertEquals("One", r4.getStartNode().getProperty("name"));
            assertEquals(1, r4.getStartNode().getProperty("count", 2));
            assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) r4.getStartNode().getProperty("tags")));
            assertEquals(3, count(r4.getStartNode().getPropertyKeys()));

            assertEquals("Three", r4.getEndNode().getProperty("name"));
            assertEquals("London", r4.getEndNode().getProperty("place"));
            assertEquals("nothing", r4.getEndNode().getProperty("tags", "nothing"));

            Node startNode = r4.getStartNode();
            Relationship r5 = startNode.getSingleRelationship(withName("R1"), OUTGOING);
            assertEquals("Two", r5.getEndNode().getProperty("name"));

            assertEquals(4, count(startNode.getRelationships()));
            assertEquals(3, count(startNode.getRelationships(OUTGOING)));
            assertEquals(2, count(startNode.getRelationships(withName("R3"))));
            assertEquals(3, count(startNode.getRelationships(withName("R3"), withName("R1"))));
            assertEquals(1, count(startNode.getRelationships(INCOMING, withName("R3"), withName("R1"))));
        }
    }

    @Test
    public void shouldBeAbleToDeleteChangedNodeCommittingTransaction() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Change<Node> change = transactionData.getChanged(database.getNodeById(1));
                        //must first delete the new relationship
                        change.getCurrent().getSingleRelationship(withName("R1"), OUTGOING).delete();
                        deleteNodeAndRelationships(change.getPrevious().getGraphDatabase().getNodeById(change.getPrevious().getId()));
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {
            assertEquals(4, countNodes(database));
        }
    }

    @Test
    public void shouldBeAbleToWipeTheGraphBeforeCommittingTransaction() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        for (Node node : database.getAllNodes()) {
                            deleteNodeAndRelationships(node);
                        }
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, countNodes(database));
        }
    }

    @Test
    public void shouldNotChangeAnythingWhenDeletingAlreadyDeletedNodeAndRelationships() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);
                        deleteNodeAndRelationships(deleted);
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {
            assertEquals(5, countNodes(database));
        }
    }

    @Test
    public void shouldBeAbleToCreateAdditionalNodesAndRelationshipsFromCurrentGraphVersion() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        Node newNode = database.createNode();
                        newNode.setProperty("name", "Six");
                        node.createRelationshipTo(newNode, withName("R4")).setProperty("new", true);
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {
            Relationship newRelationship = database.getNodeById(1).getSingleRelationship(withName("R4"), OUTGOING);
            assertNotNull(newRelationship);
            assertEquals("Six", newRelationship.getEndNode().getProperty("name"));
            assertEquals(true, newRelationship.getProperty("new"));
        }
    }

    @Test
    public void shouldBeAbleToCreateAdditionalNodesAndRelationshipsFromPreviousGraphVersion() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        Node newNode = database.createNode();
                        newNode.setProperty("name", "Six");
                        node.createRelationshipTo(newNode, withName("R4")).setProperty("new", true);
                    }
                }
        );

        try (Transaction tx = database.beginTx()) {
            Relationship newRelationship = database.getNodeById(1).getSingleRelationship(withName("R4"), OUTGOING);
            assertNotNull(newRelationship);
            assertEquals("Six", newRelationship.getEndNode().getProperty("name"));
            assertEquals(true, newRelationship.getProperty("new"));
        }
    }

    @Test
    public void propertyExtractionStrategySmokeTest() {
        createTestDatabase();
        final AtomicLong changedRelId = new AtomicLong();
        try (Transaction tx = database.beginTx()) {
            changedRelId.set(database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING).getId());
        }
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Relationship previous = transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getPrevious();
                        Relationship current = transactionData.getChanged(database.getRelationshipById(changedRelId.get())).getCurrent();

                        Map<String, Object> previousProps = new OtherNodeNameIncludingRelationshipPropertiesExtractor().extractProperties(previous, previous.getStartNode());
                        assertEquals(2, previousProps.size());
                        assertEquals("One", previousProps.get("otherNodeName"));
                        assertEquals("cool", previousProps.get("tag"));

                        Map<String, Object> currentProps = new OtherNodeNameIncludingRelationshipPropertiesExtractor().extractProperties(current, current.getStartNode());
                        assertEquals(2, currentProps.size());
                        assertEquals("NewOne", currentProps.get("otherNodeName"));
                        assertEquals("cool", currentProps.get("tags"));
                    }
                }
        );
    }

    @Test
    public void shouldIndicateNoMutationWhenNothingHasBeenChanged() {
        createTestDatabase();
        mutateGraph(new VoidReturningCallback() {
                        @Override
                        protected void doInTx(GraphDatabaseService database) {
                            //change that should not be picked up as a change
                            Node four = database.getNodeById(4);
                            four.setProperty("name", "Three");
                            four.setProperty("name", "Four");
                        }
                    }, new BeforeCommitCallback() {
                        @Override
                        public void doBeforeCommit(ImprovedTransactionData transactionData) {
                            assertFalse(transactionData.mutationsOccurred());
                        }
                    }
        );
    }

    //test helpers

    private void mutateGraph(BeforeCommitCallback beforeCommitCallback) {
        mutateGraph(new TestGraphMutation(), beforeCommitCallback);
    }

    private void mutateGraph(VoidReturningCallback transactionCallback, BeforeCommitCallback beforeCommitCallback) {
        mutateGraph(transactionCallback, beforeCommitCallback, RethrowException.getInstance());
    }

    private void mutateGraph(TransactionCallback<Void> transactionCallback, BeforeCommitCallback beforeCommitCallback, ExceptionHandlingStrategy exceptionHandlingStrategy) {
        TestingTxEventHandler handler = new TestingTxEventHandler(beforeCommitCallback);
        database.registerTransactionEventHandler(handler);
        new SimpleTransactionExecutor(database).executeInTransaction(transactionCallback, exceptionHandlingStrategy);
    }

    private class TestingTxEventHandler implements TransactionEventHandler {

        private final BeforeCommitCallback beforeCommitCallback;

        private TestingTxEventHandler(BeforeCommitCallback beforeCommitCallback) {
            this.beforeCommitCallback = beforeCommitCallback;
        }

        @Override
        public Object beforeCommit(TransactionData data) throws Exception {
            LazyTransactionData lazyTransactionData = new LazyTransactionData(data);

            beforeCommitCallback.doBeforeCommit(new FilteredTransactionData(lazyTransactionData, new InclusionPolicies(
                    new BaseNodeInclusionPolicy() {
                        @Override
                        public boolean include(Node node) {
                            return !node.getProperty("name", "").equals("Four") && !node.hasProperty(INTERNAL_NODE_PROPERTY);
                        }
                    },
                    new NodePropertyInclusionPolicy() {
                        @Override
                        public boolean include(String key, Node propertyContainer) {
                            return !"place".equals(key) && !key.startsWith(INTERNAL_PREFIX);
                        }
                    },
                    new RelationshipInclusionPolicy.Adapter() {
                        @Override
                        public boolean include(Relationship relationship) {
                            return !relationship.isType(withName("R3")) && !relationship.getType().name().startsWith(INTERNAL_PREFIX);
                        }
                    }
                    , new RelationshipPropertyInclusionPolicy() {
                @Override
                public boolean include(String key, Relationship propertyContainer) {
                    return !"time".equals(key) && !key.startsWith(INTERNAL_PREFIX);
                }
            }
            )));

            return null;
        }

        @Override
        public void afterCommit(TransactionData data, Object state) {
            //do nothing
        }

        @Override
        public void afterRollback(TransactionData data, Object state) {
            //do nothing
        }
    }

    private interface BeforeCommitCallback {
        void doBeforeCommit(ImprovedTransactionData transactionData);
    }

    private class OtherNodeNameIncludingRelationshipPropertiesExtractor {

        public Map<String, Object> extractProperties(Relationship relationship, Node pointOfView) {
            Map<String, Object> result = new HashMap<>();
            result.putAll(propertiesToMap(relationship));
            result.put("otherNodeName", relationship.getOtherNode(pointOfView).getProperty("name").toString());
            return result;
        }
    }

    private void createTestDatabase() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);

        new TestDataBuilder(database)
                .node() //ID=0
                .node().setProp("name", "One").setProp("count", 1).setProp("tags", new String[]{"one", "two"})

                .node().setProp("name", "Two").setProp("size", 2L)
                .relationshipFrom(1, "R1").setProp("time", 1)
                .relationshipFrom(2, "R2")

                .node().setProp("name", "Three").setProp("place", "London")
                .relationshipFrom(2, "R2").setProp("time", 2)
                .relationshipTo(1, "R3").setProp("time", 3).setProp("tag", "cool")
                .relationshipFrom(1, "R3")

                .node().setProp("name", "Four")
                .relationshipFrom(3, "R1").setProp("time", 1)
                .relationshipFrom(1, "WHATEVER");
    }

    private class TestGraphMutation extends VoidReturningCallback {

        @Override
        public void doInTx(GraphDatabaseService database) {
            Node one = database.getNodeById(1);
            one.setProperty("name", "NewOne");
            one.removeProperty("count");
            one.setProperty("tags", new String[]{"one"});
            one.setProperty("tags", new String[]{"one", "three"});

            Node two = database.getNodeById(2);
            deleteNodeAndRelationships(two);

            Node three = database.getNodeById(3);
            three.setProperty("tags", "one");
            three.setProperty("place", "Rome");
            three.setProperty("place", "London");

            Node five = database.createNode();
            five.setProperty("name", "Five");
            five.setProperty("size", 3L);
            five.setProperty("size", 4L);
            Relationship r = five.createRelationshipTo(three, withName("R2"));
            r.setProperty("time", 4);

            r = three.getSingleRelationship(withName("R3"), OUTGOING);
            r.setProperty("time", 4);
            r.removeProperty("tag");
            r.setProperty("tags", "cool");

            three.getSingleRelationship(withName("R3"), INCOMING).delete();

            one.createRelationshipTo(three, withName("R1"));

            //change that should not be picked up as a change
            Node four = database.getNodeById(4);
            four.setProperty("name", "Three");
            four.setProperty("name", "Four");
        }
    }

    private void createTestDatabaseForInternalTest() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);

        new TestDataBuilder(database)
                .node() //ID=0
                .node().setProp(INTERNAL_NODE_PROPERTY, "whatever").setProp(INTERNAL_PREFIX + "name", "One").setProp(INTERNAL_PREFIX + "count", 1).setProp(INTERNAL_PREFIX + "tags", new String[]{"one", "two"})

                .node().setProp(INTERNAL_NODE_PROPERTY, "whatever").setProp("name", "Two").setProp("size", 2L)
                .relationshipFrom(1, INTERNAL_PREFIX + "R1").setProp("time", 1)
                .relationshipFrom(2, INTERNAL_PREFIX + "R2")

                .node().setProp("name", "Three").setProp("place", "London")
                .relationshipFrom(2, INTERNAL_PREFIX + "R2").setProp("time", 2)
                .relationshipTo(1, "R4").setProp(INTERNAL_PREFIX + "time", 3).setProp(INTERNAL_PREFIX + "tag", "cool")
                .relationshipFrom(1, INTERNAL_PREFIX + "R3")

                .node().setProp(INTERNAL_NODE_PROPERTY, "whatever").setProp("name", "Four")
                .relationshipFrom(3, INTERNAL_PREFIX + "R1").setProp("time", 1);
    }

    private class InternalTestGraphMutation extends VoidReturningCallback {

        @Override
        public void doInTx(GraphDatabaseService database) {
            Node one = database.getNodeById(1);
            one.setProperty(INTERNAL_PREFIX + "name", "NewOne");
            one.removeProperty(INTERNAL_PREFIX + "count");
            one.setProperty(INTERNAL_PREFIX + "tags", new String[]{"one"});
            one.setProperty(INTERNAL_PREFIX + "tags", new String[]{"one", "three"});

            Node two = database.getNodeById(2);
            deleteNodeAndRelationships(two);

            Node three = database.getNodeById(3);
            three.setProperty(INTERNAL_PREFIX + "tags", "one");
            three.setProperty("place", "Rome");
            three.setProperty("place", "London");

            Node five = database.createNode();
            five.setProperty("name", "Five");
            five.setProperty("size", 3L);
            five.setProperty("size", 4L);
            five.setProperty(INTERNAL_NODE_PROPERTY, "anything");
            Relationship r = five.createRelationshipTo(three, withName(INTERNAL_PREFIX + "R2"));
            r.setProperty("time", 4);

            r = three.getSingleRelationship(withName("R4"), OUTGOING);
            r.setProperty(INTERNAL_PREFIX + "time", 4);
            r.removeProperty(INTERNAL_PREFIX + "tag");
            r.setProperty(INTERNAL_PREFIX + "tags", "cool");

            three.getSingleRelationship(withName(INTERNAL_PREFIX + "R3"), INCOMING).delete();

            one.createRelationshipTo(three, withName(INTERNAL_PREFIX + "R1"));

            //change that should not be picked up as a change
            Node four = database.getNodeById(4);
            four.setProperty("name", "Three");
            four.setProperty("name", "Four");
        }
    }

    private class LabelChange extends VoidReturningCallback {

        @Override
        public void doInTx(GraphDatabaseService database) {
            Node michal = (database.findNode(label("Person"), "name", "Michal"));
            michal.removeLabel(label("Person"));
            michal.addLabel(label("Human"));
        }
    }

    private class Holder<T> {
        private T held;

        private T get() {
            return held;
        }

        private void set(T held) {
            this.held = held;
        }
    }
}
