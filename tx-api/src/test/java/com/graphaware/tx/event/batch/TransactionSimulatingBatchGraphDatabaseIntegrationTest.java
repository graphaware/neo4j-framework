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

package com.graphaware.tx.event.batch;

import com.graphaware.test.util.TestDataBuilder;
import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import com.graphaware.tx.executor.single.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.common.util.PropertyContainerUtils.propertiesToMap;
import static com.graphaware.common.util.PropertyContainerUtils.propertyContainersToMap;
import static com.graphaware.tx.event.improved.api.Change.*;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Integration test for {@link org.neo4j.unsafe.batchinsert.TransactionSimulatingBatchGraphDatabase}.
 */
@SuppressWarnings("deprecation")
public class TransactionSimulatingBatchGraphDatabaseIntegrationTest {

    private GraphDatabaseService database;
    private TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void createTemporaryFolder() throws IOException {
        temporaryFolder.create();
    }

    @After
    public void tearDown() {
        database.shutdown();
        temporaryFolder.delete();
    }

    @Test
    public void shouldBeAbleToSetPropertyOnRoot() {
        String dir = temporaryFolder.getRoot().getAbsolutePath();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(dir));
        database.createNode();

        Node root = database.getNodeById(0);
        root.setProperty("test", "test");

        assertEquals("test", root.getProperty("test"));
    }

    @Test
    public void nothingShouldEverBeDeleted() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        assertFalse(transactionData.hasBeenDeleted(database.getNodeById(1)));
                        assertFalse(transactionData.hasBeenDeleted(database.getRelationshipById(1)));
                    }
                }
        );
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

                        long r1Id = database.getNodeById(5).getSingleRelationship(withName("R5"), OUTGOING).getId();
                        Relationship r1 = created.get(r1Id);
                        assertEquals(4, r1.getProperty("time"));
                        assertEquals(1, count(r1.getPropertyKeys()));

                        long r2Id = database.getNodeById(1).getSingleRelationship(withName("R4"), OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);
                        assertEquals(0, count(r2.getPropertyKeys()));

                        assertTrue(transactionData.hasBeenCreated(r1));
                        assertTrue(transactionData.hasBeenCreated(r2));
                        assertFalse(transactionData.hasBeenCreated(database.getNodeById(3).getSingleRelationship(withName("R1"), OUTGOING)));

                        //in contrast to filtered version:
                        assertTrue(r2.getEndNode().hasProperty("place"));
                        assertNotNull(r2.getEndNode().getSingleRelationship(withName("R3"), OUTGOING));

                        //directions:
                        Node startNode = r1.getStartNode();
                        Node endNode = r1.getEndNode();
                        assertEquals(startNode, r1.getOtherNode(endNode));
                        assertEquals(endNode, r1.getOtherNode(startNode));
                        assertEquals(startNode, r1.getNodes()[0]);
                        assertEquals(endNode, r1.getNodes()[1]);

                        try {
                            r1.getOtherNode(database.getNodeById(4));
                            fail();
                        } catch (IllegalArgumentException e) {
                            //OK
                        }

                        //type:
                        assertTrue(r1.isType(withName("R5")));
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

                        long r2Id = database.getNodeById(1).getSingleRelationship(withName("R4"), OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);

                        assertEquals("NewOne", r2.getStartNode().getProperty("name"));
                        assertFalse(r2.getStartNode().hasProperty("count"));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) r2.getStartNode().getProperty("tags")));

                        assertNotNull(r2.getEndNode().getSingleRelationship(withName("R3"), INCOMING));
                        assertEquals(2, r2.getEndNode().getSingleRelationship(withName("R2"), INCOMING).getStartNode().getId());
                        assertEquals(5, r2.getEndNode().getSingleRelationship(withName("R5"), INCOMING).getStartNode().getId());
                    }
                }
        );
    }

    @Test
    public void changedRelationshipsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        assertEquals(1, changed.size());

                        Relationship previous = changed.entrySet().iterator().next().getValue().getPrevious();
                        assertEquals(2, count(previous.getPropertyKeys()));
                        assertEquals(3, previous.getProperty("time"));
                        assertEquals("cool", previous.getProperty("tag"));

                        Relationship current = changed.entrySet().iterator().next().getValue().getCurrent();
                        assertEquals(2, count(current.getPropertyKeys()));
                        assertEquals(4, current.getProperty("time"));
                        assertEquals("cool", current.getProperty("tags"));

                        assertTrue(transactionData.hasBeenChanged(previous));
                        assertTrue(transactionData.hasBeenChanged(current));
                        assertFalse(transactionData.hasBeenChanged(database.getNodeById(3).getSingleRelationship(withName("R1"), OUTGOING)));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedRelationshipPreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship previous = changed.entrySet().iterator().next().getValue().getPrevious();

                        assertEquals("One", previous.getEndNode().getProperty("name"));
                        assertEquals(1, previous.getEndNode().getProperty("count", 2));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) previous.getEndNode().getProperty("tags")));
                        assertEquals(4, count(previous.getEndNode().getPropertyKeys()));

                        assertEquals("Three", previous.getStartNode().getProperty("name"));
                        assertEquals("London", previous.getStartNode().getProperty("place"));
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
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship previous = changed.entrySet().iterator().next().getValue().getPrevious();

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
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
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship current = changed.entrySet().iterator().next().getValue().getCurrent();

                        current = transactionData.getChanged(current).getCurrent();

                        assertEquals("NewOne", current.getEndNode().getProperty("name"));
                        assertEquals(2, current.getEndNode().getProperty("count", 2));
                        assertFalse(current.getEndNode().hasProperty("count"));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) current.getEndNode().getProperty("tags")));
                        assertEquals(2, count(current.getEndNode().getPropertyKeys()));

                        assertEquals("Three", current.getStartNode().getProperty("name"));
                        assertEquals("London", current.getStartNode().getProperty("place"));
                        assertEquals("one", current.getStartNode().getProperty("tags"));
                    }
                }
        );
    }

    @Test
    public void startingWithCurrentChangedRelationshipCurrentGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship current = changed.entrySet().iterator().next().getValue().getCurrent();

                        TraversalDescription traversalDescription = database.traversalDescription()
                                .relationships(withName("R1"), OUTGOING)
                                .relationships(withName("R2"), OUTGOING)
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL);

                        assertEquals(4, count(traversalDescription.traverse(current.getEndNode()).nodes()));
                    }
                }
        );
    }

    @Test
    public void deletedRelationshipsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> deleted = propertyContainersToMap(transactionData.getAllDeletedRelationships());
                        assertEquals(0, deleted.size());  //no deletes in batch
                    }
                }
        );
    }

    @Test
    public void createdRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getAllChangedRelationships().iterator().next();

                        assertTrue(transactionData.hasPropertyBeenCreated(change.getCurrent(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenCreated(change.getCurrent(), "time"));
                        assertTrue(transactionData.hasPropertyBeenCreated(change.getPrevious(), "tags"));
                        assertFalse(transactionData.hasPropertyBeenCreated(change.getPrevious(), "tag"));

                        assertEquals(1, transactionData.createdProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.createdProperties(change.getPrevious()).size());
                        assertEquals("cool", transactionData.createdProperties(change.getCurrent()).get("tags"));
                        assertEquals("cool", transactionData.createdProperties(change.getPrevious()).get("tags"));

                        //created relationship should not fall into this category
                        assertFalse(transactionData.hasPropertyBeenCreated(database.getNodeById(5).getSingleRelationship(withName("R5"), OUTGOING), "time"));
                    }
                }
        );
    }

    @Test
    public void changedRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getAllChangedRelationships().iterator().next();

                        assertTrue(transactionData.hasPropertyBeenChanged(change.getCurrent(), "time"));
                        assertFalse(transactionData.hasPropertyBeenChanged(change.getCurrent(), "tags"));
                        assertTrue(transactionData.hasPropertyBeenChanged(change.getPrevious(), "time"));
                        assertFalse(transactionData.hasPropertyBeenChanged(change.getPrevious(), "tag"));

                        assertEquals(1, transactionData.changedProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.changedProperties(change.getPrevious()).size());
                        assertEquals(3, transactionData.changedProperties(change.getCurrent()).get("time").getPrevious());
                        assertEquals(3, transactionData.changedProperties(change.getPrevious()).get("time").getPrevious());
                        assertEquals(4, transactionData.changedProperties(change.getCurrent()).get("time").getCurrent());
                        assertEquals(4, transactionData.changedProperties(change.getPrevious()).get("time").getCurrent());
                    }
                }
        );
    }

    @Test
    public void deletedRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getAllChangedRelationships().iterator().next();

                        assertTrue(transactionData.hasPropertyBeenDeleted(change.getCurrent(), "tag"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(change.getCurrent(), "time"));
                        assertTrue(transactionData.hasPropertyBeenDeleted(change.getPrevious(), "tag"));
                        assertFalse(transactionData.hasPropertyBeenDeleted(change.getPrevious(), "tags"));

                        assertEquals(1, transactionData.deletedProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.deletedProperties(change.getPrevious()).size());
                        assertEquals("cool", transactionData.deletedProperties(change.getCurrent()).get("tag"));
                        assertEquals("cool", transactionData.deletedProperties(change.getPrevious()).get("tag"));

                        assertFalse(transactionData.hasPropertyBeenDeleted(transactionData.getAllCreatedRelationships().iterator().next(), "tags"));
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
                        assertEquals("Five", createdNode.getProperty("name"));
                        assertEquals(4L, createdNode.getProperty("size"));
                        assertEquals(2, count(createdNode.getPropertyKeys()));
                        try {
                            createdNode.getProperty("non-existing");
                            fail();
                        } catch (NotFoundException e) {
                            //ok
                        }

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

                        assertEquals("one", createdNode.getSingleRelationship(withName("R5"), OUTGOING).getEndNode().getProperty("tags"));
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
                        assertEquals(4, count(previous1.getPropertyKeys()));
                        assertEquals("One", previous1.getProperty("name"));
                        assertEquals(1, previous1.getProperty("count"));
                        assertEquals("something", previous1.getProperty("something"));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) previous1.getProperty("tags")));

                        Node current1 = changed.get(1L).getCurrent();
                        assertEquals(2, count(current1.getPropertyKeys()));
                        assertEquals("NewOne", current1.getProperty("name"));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) current1.getProperty("tags")));

                        Node previous2 = changed.get(3L).getPrevious();
                        assertEquals(2, count(previous2.getPropertyKeys()));
                        assertEquals("Three", previous2.getProperty("name"));
                        assertEquals("London", previous2.getProperty("place"));

                        Node current2 = changed.get(3L).getCurrent();
                        assertEquals(3, count(current2.getPropertyKeys()));
                        assertEquals("Three", current2.getProperty("name"));
                        assertEquals("London", current2.getProperty("place"));
                        assertEquals("one", current2.getProperty("tags"));

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

                        assertEquals(1, previous.getSingleRelationship(withName("R1"), OUTGOING).getProperty("time"));
                        assertEquals("Two", previous.getRelationships(withName("R1"), OUTGOING).iterator().next().getEndNode().getProperty("name"));
                        assertEquals("Three", previous.getRelationships(OUTGOING, withName("R3")).iterator().next().getEndNode().getProperty("name"));
                        assertEquals(2L, previous.getRelationships(withName("R1")).iterator().next().getEndNode().getProperty("size"));

                        assertNull(previous.getSingleRelationship(withName("R1"), INCOMING));
                        assertEquals(4, count(previous.getRelationships()));
                        assertEquals(3, count(previous.getRelationships(withName("R1"), withName("R3"))));
                        assertEquals(1, count(previous.getRelationships(withName("R1"))));
                        assertEquals(3, count(previous.getRelationships(OUTGOING)));
                        assertEquals(1, count(previous.getRelationships(INCOMING)));
                        assertEquals(1, count(previous.getRelationships(OUTGOING, withName("R3"))));
                        assertEquals(2, count(previous.getRelationships(OUTGOING, withName("R1"), withName("R3"))));
                        assertEquals(1, count(previous.getRelationships(withName("R3"), OUTGOING)));

                        assertTrue(previous.hasRelationship());
                        assertTrue(previous.hasRelationship(withName("R1"), withName("R3")));
                        assertTrue(previous.hasRelationship(withName("R1")));
                        assertTrue(previous.hasRelationship(OUTGOING));
                        assertTrue(previous.hasRelationship(INCOMING));
                        assertTrue(previous.hasRelationship(OUTGOING, withName("R3")));
                        assertTrue(previous.hasRelationship(OUTGOING, withName("R1"), withName("R3")));
                        assertTrue(previous.hasRelationship(withName("R3"), OUTGOING));

                        assertFalse(previous.hasRelationship(withName("R1"), INCOMING));
                        assertFalse(previous.hasRelationship(withName("R2")));

                        previous.createRelationshipTo(database.getNodeById(4), withName("R3"));
                        try {
                            previous.getSingleRelationship(withName("R3"), OUTGOING);
                            fail();
                        } catch (NotFoundException e) {
                            //ok
                        }
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

                        assertEquals("Three", current.getSingleRelationship(withName("R4"), OUTGOING).getEndNode().getProperty("name"));
                        assertEquals("London", current.getRelationships(withName("R4"), OUTGOING).iterator().next().getEndNode().getProperty("place"));
                        assertEquals("one", current.getRelationships(OUTGOING, withName("R4")).iterator().next().getEndNode().getProperty("tags"));
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
                                .depthFirst()
                                .uniqueness(Uniqueness.NODE_GLOBAL);

                        assertEquals(4, count(traversalDescription.traverse(current).nodes()));

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

                        assertEquals(4, count(changed.getPrevious().getPropertyKeys()));
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

                        assertFalse(transactionData.hasPropertyBeenChanged(transactionData.getAllCreatedNodes().iterator().next(), "name"));

                        assertEquals(2, count(changed.getPrevious().getPropertyKeys()));
                        assertEquals(3, count(changed.getCurrent().getPropertyKeys()));

                        //one that isn't changed
                        Node unchanged = changesToMap(transactionData.getAllChangedNodes()).get(1L).getPrevious().getSingleRelationship(withName("R3"), OUTGOING).getEndNode().getSingleRelationship(withName("R1"), OUTGOING).getEndNode();
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

                        assertEquals(2, transactionData.deletedProperties(changed.getCurrent()).size());
                        assertEquals(2, transactionData.deletedProperties(changed.getPrevious()).size());
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
                        Map<Long, Relationship> created = propertyContainersToMap(transactionData.getAllCreatedRelationships());

                        long r1Id = database.getNodeById(5).getSingleRelationship(withName("R5"), OUTGOING).getId();
                        Relationship r1 = created.get(r1Id);

                        r1.setProperty("additional", "someValue");
                        r1.removeProperty("time");
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Relationship r1 = database.getNodeById(5).getSingleRelationship(withName("R5"), OUTGOING);
        assertEquals(1, count(r1.getPropertyKeys()));
        assertEquals("someValue", r1.getProperty("additional"));
        assertFalse(r1.hasProperty("time"));
    }

    @Test
    public void shouldBeAbleToChangeCreatedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> createdNodes = propertyContainersToMap(transactionData.getAllCreatedNodes());
                        Node createdNode = createdNodes.get(5L);

                        createdNode.setProperty("name", "NewFive");
                        createdNode.setProperty("additional", "something");
                        createdNode.removeProperty("size");
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Node createdNode = database.getNodeById(5L);

        assertEquals("NewFive", createdNode.getProperty("name"));
        assertEquals("something", createdNode.getProperty("additional"));
        assertEquals(2, count(createdNode.getPropertyKeys()));
        assertFalse(createdNode.hasProperty("size"));
    }

    @Test
    public void shouldBeAbleToChangeCurrentChangedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());

                        Relationship r = changed.entrySet().iterator().next().getValue().getCurrent();

                        r.setProperty("time", 5);
                        r.setProperty("additional", "something");
                        r.removeProperty("tags");
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Relationship r = database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING);

        assertEquals(2, count(r.getPropertyKeys()));
        assertEquals(5, r.getProperty("time"));
        assertEquals("something", r.getProperty("additional"));
        assertFalse(r.hasProperty("tags"));
    }

    @Test
    public void shouldBeAbleToChangePreviousChangedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());

                        Relationship r = changed.entrySet().iterator().next().getValue().getPrevious();

                        r.setProperty("time", 5);
                        r.setProperty("additional", "something");
                        r.removeProperty("tags");
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Relationship r = database.getNodeById(3).getSingleRelationship(withName("R3"), OUTGOING);

        assertEquals(2, count(r.getPropertyKeys()));
        assertEquals(5, r.getProperty("time"));
        assertEquals("something", r.getProperty("additional"));
        assertFalse(r.hasProperty("tags"));
    }

    @Test
    public void shouldBeAbleToChangeCurrentChangedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        node.setProperty("name", "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty("tags");
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Node node = database.getNodeById(1L);

        assertEquals(2, count(node.getPropertyKeys()));
        assertEquals("YetAnotherOne", node.getProperty("name"));
        assertEquals("something", node.getProperty("additional"));
        assertFalse(node.hasProperty("tags"));
    }

    @Test
    public void shouldBeAbleToChangePreviousChangedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        node.setProperty("name", "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty("tags");
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Node node = database.getNodeById(1L);

        assertEquals(2, count(node.getPropertyKeys()));
        assertEquals("YetAnotherOne", node.getProperty("name"));
        assertEquals("something", node.getProperty("additional"));
        assertFalse(node.hasProperty("tags"));
    }

    @Test
    public void shouldBeAbleToCreateAdditionalNodesAndRelationshipsFromCurrentGraphVersion() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        Node newNode = database.createNode();
                        newNode.setProperty("name", "Six");
                        node.createRelationshipTo(newNode, withName("R6")).setProperty("new", true);

                        //just a check we can see this right away
                        Relationship newRelationship = database.getNodeById(1).getSingleRelationship(withName("R6"), OUTGOING);
                        assertNotNull(newRelationship);
                        assertEquals("Six", newRelationship.getEndNode().getProperty("name"));
                        assertEquals(true, newRelationship.getProperty("new"));
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Relationship newRelationship = database.getNodeById(1).getSingleRelationship(withName("R6"), OUTGOING);
        assertNotNull(newRelationship);
        assertEquals("Six", newRelationship.getEndNode().getProperty("name"));
        assertEquals(true, newRelationship.getProperty("new"));
    }

    @Test
    public void shouldBeAbleToCreateAdditionalNodesAndRelationshipsFromPreviousGraphVersion() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        Node newNode = database.createNode();
                        newNode.setProperty("name", "Six");
                        node.createRelationshipTo(newNode, withName("R6")).setProperty("new", true);
                    }
                }
        );

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Relationship newRelationship = database.getNodeById(1).getSingleRelationship(withName("R6"), OUTGOING);
        assertNotNull(newRelationship);
        assertEquals("Six", newRelationship.getEndNode().getProperty("name"));
        assertEquals(true, newRelationship.getProperty("new"));
    }

    @Test
    public void propertyExtractionStrategySmokeTest() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Relationship previous = transactionData.getAllChangedRelationships().iterator().next().getPrevious();
                        Relationship current = transactionData.getAllChangedRelationships().iterator().next().getCurrent();

                        Map<String, Object> previousProps = new OtherNodeNameIncludingRelationshipPropertiesExtractor().extractProperties(previous, previous.getStartNode());
                        assertEquals(3, previousProps.size());
                        assertEquals("One", previousProps.get("otherNodeName"));
                        assertEquals(3, previousProps.get("time"));
                        assertEquals("cool", previousProps.get("tag"));

                        Map<String, Object> currentProps = new OtherNodeNameIncludingRelationshipPropertiesExtractor().extractProperties(current, current.getStartNode());
                        assertEquals(3, currentProps.size());
                        assertEquals("NewOne", currentProps.get("otherNodeName"));
                        assertEquals(4, currentProps.get("time"));
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

    @Test
    public void verifyRelationshipRetrieving() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertEquals(5, count(database.getNodeById(1).getRelationships()));
                        assertEquals(2, count(database.getNodeById(1).getRelationships(withName("R3"))));
                        assertEquals(3, count(database.getNodeById(1).getRelationships(withName("R3"), withName("R1"))));
                        assertEquals(4, count(database.getNodeById(1).getRelationships(OUTGOING)));
                        assertEquals(1, count(database.getNodeById(1).getRelationships(OUTGOING, withName("R3"))));
                        assertEquals(1, count(database.getNodeById(1).getRelationships(withName("R3"), OUTGOING)));

                        assertTrue(database.getNodeById(1).hasRelationship());
                        assertTrue(database.getNodeById(1).hasRelationship(withName("R3")));
                        assertTrue(database.getNodeById(1).hasRelationship(withName("R3"), withName("R1")));
                        assertTrue(database.getNodeById(1).hasRelationship(OUTGOING));
                        assertTrue(database.getNodeById(1).hasRelationship(OUTGOING, withName("R3")));
                        assertTrue(database.getNodeById(1).hasRelationship(withName("R3"), OUTGOING));

                        assertFalse(database.getNodeById(1).hasRelationship(withName("R")));
                        assertFalse(database.getNodeById(1).hasRelationship(withName("R"), withName("R")));
                        assertFalse(database.getNodeById(1).hasRelationship(OUTGOING, withName("R")));
                        assertFalse(database.getNodeById(1).hasRelationship(withName("R"), OUTGOING));
                    }
                }
        );
    }

    @Test
    public void verifyAutoCommit() {
        String dir = temporaryFolder.getRoot().getAbsolutePath();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(dir));

        populateDatabase();

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(dir), 27);

        final AtomicInteger numberOfCommits = new AtomicInteger(0);

        mutateGraph(
                new BeforeCommitCallback() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        numberOfCommits.incrementAndGet();
                        assertEquals(2, transactionData.getAllCreatedRelationships().size());
                        assertEquals(1, transactionData.getAllChangedRelationships().size());
                        assertEquals(0, transactionData.getAllDeletedRelationships().size());

                        assertEquals(1, transactionData.getAllCreatedNodes().size());
                        assertEquals(2, transactionData.getAllChangedNodes().size());
                        assertEquals(0, transactionData.getAllDeletedNodes().size());
                    }
                }
        );

        database.shutdown();

        assertEquals(1, numberOfCommits.get());

        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(dir));
    }

    @Test
    public void shouldReturnNoNodesWhenNothingInTheDatabase() {
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        assertFalse(database.getAllNodes().iterator().hasNext());
    }

    @Test
    public void shouldReturnAllNodesFromTheDatabase() {
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(temporaryFolder.getRoot().getAbsolutePath()));

        Node node1 = database.createNode();
        Node node2 = database.createNode();

        node1.setProperty("name", "One");
        node2.setProperty("name", "Two");

        Iterable<Node> allNodes = database.getAllNodes();
        assertEquals(2, count(allNodes));

        Iterator<Node> iterator = database.getAllNodes().iterator();
        assertEquals("One", iterator.next().getProperty("name"));
        assertEquals("Two", iterator.next().getProperty("name"));
    }

    @Test
    public void shouldReturnAllNodesFromTheDatabaseWithGaps() {
        String dir = temporaryFolder.getRoot().getAbsolutePath();
        database = new GraphDatabaseFactory().newEmbeddedDatabase(dir);

        new SimpleTransactionExecutor(database).executeInTransaction(new VoidReturningCallback() {
            @Override
            protected void doInTx(GraphDatabaseService database) {
                Node node1 = database.createNode();
                Node node2 = database.createNode();
                Node node3 = database.createNode();

                node1.setProperty("name", "One");
                node2.setProperty("name", "Two");
                node3.setProperty("name", "Three");

                node2.delete();
            }
        });

        database.shutdown();

        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(dir));

        Iterable<Node> allNodes = database.getAllNodes();
        assertEquals(2, count(allNodes));

        Iterator<Node> iterator = database.getAllNodes().iterator();
        assertEquals("One", iterator.next().getProperty("name"));
        assertEquals("Three", iterator.next().getProperty("name"));
    }

    //test helpers

    protected void mutateGraph(BeforeCommitCallback beforeCommitCallback) {
        mutateGraph(new TestGraphMutation(), beforeCommitCallback);
    }

    protected void mutateGraph(VoidReturningCallback transactionCallback, BeforeCommitCallback beforeCommitCallback) {
        mutateGraph(transactionCallback, beforeCommitCallback, RethrowException.getInstance());
    }

    protected void mutateGraph(TransactionCallback<Void> transactionCallback, BeforeCommitCallback beforeCommitCallback, ExceptionHandlingStrategy exceptionHandlingStrategy) {
        TestingTxEventHandler handler = new TestingTxEventHandler(beforeCommitCallback);
        database.registerTransactionEventHandler(handler);
        try {
            transactionCallback.doInTransaction(database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class TestingTxEventHandler implements TransactionEventHandler {

        private final BeforeCommitCallback beforeCommitCallback;

        private TestingTxEventHandler(BeforeCommitCallback beforeCommitCallback) {
            this.beforeCommitCallback = beforeCommitCallback;
        }

        @Override
        public Object beforeCommit(TransactionData data) throws Exception {
            beforeCommitCallback.doBeforeCommit(new LazyTransactionData(data));
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
        String dir = temporaryFolder.getRoot().getAbsolutePath();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(dir));

        populateDatabase();

        database.shutdown();
        database = new TransactionSimulatingBatchGraphDatabase(BatchInserters.batchDatabase(dir));
    }

    private void populateDatabase() {
        new TestDataBuilder(database)
                .node()
                .node().setProp("name", "One").setProp("count", 1).setProp("tags", new String[]{"one", "two"}).setProp("something", "something")
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
            one.setProperty("count", "something");
            one.removeProperty("count");
            one.removeProperty("something");
            one.setProperty("tags", new String[]{"one"});
            one.setProperty("tags", new String[]{"one", "three"});

            Node three = database.getNodeById(3);
            three.setProperty("tags", "one");
            three.setProperty("place", "Rome");
            three.setProperty("place", "London");
            three.removeProperty("place");
            three.removeProperty("place");
            three.setProperty("place", "London");

            Node five = database.createNode();
            five.setProperty("name", "Five");
            five.setProperty("size", 3L);
            five.setProperty("size", 4L);
            Relationship r = five.createRelationshipTo(three, withName("R5"));
            r.setProperty("time", 4);

            r = three.getSingleRelationship(withName("R3"), OUTGOING);
            r.setProperty("time", 4);
            r.removeProperty("tag");
            r.removeProperty("tag");
            r.setProperty("tag", "bla");
            r.removeProperty("tag");
            r.setProperty("tags", "cool");
            r.setProperty("tags", "cool");

            one.createRelationshipTo(three, withName("R4"));

            //change that should not be picked up as a change
            Node four = database.getNodeById(4);
            four.setProperty("name", "Three");
            four.setProperty("name", "Four");
        }
    }
}
