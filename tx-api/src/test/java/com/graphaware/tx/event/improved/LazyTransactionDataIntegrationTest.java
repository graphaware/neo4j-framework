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

package com.graphaware.tx.event.improved;

import com.graphaware.test.util.TestDataBuilder;
import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import com.graphaware.tx.executor.single.*;
import org.junit.After;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.graphaware.common.util.IterableUtils.contains;
import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.common.util.IterableUtils.countNodes;
import static com.graphaware.common.util.PropertyContainerUtils.*;
import static com.graphaware.tx.event.improved.LazyTransactionDataIntegrationTest.RelationshipTypes.*;
import static com.graphaware.tx.event.improved.api.Change.*;
import static junit.framework.Assert.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicLabel.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Integration test for {@link com.graphaware.tx.event.improved.api.LazyTransactionData}.
 */
@SuppressWarnings("deprecation")
public class LazyTransactionDataIntegrationTest {

    public static final String TIME = "time";
    public static final String PLACE = "place";
    public static final String NAME = "name";
    public static final String COUNT = "count";
    public static final String TAGS = "tags";
    public static final String TAG = "tag";

    public static enum RelationshipTypes implements RelationshipType {
        R1, R2, R3
    }

    private GraphDatabaseService db;

    @After
    public void tearDown() {
        db.shutdown();
    }

    @Test
    public void createdRelationshipsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> created = propertyContainersToMap(transactionData.getAllCreatedRelationships());
                        assertEquals(2, created.size());

                        long r1Id = db.getNodeById(7).getSingleRelationship(R2, OUTGOING).getId();
                        Relationship r1 = created.get(r1Id);
                        assertEquals(4, r1.getProperty(TIME));
                        assertEquals(1, count(r1.getPropertyKeys()));

                        long r2Id = db.getNodeById(1).getSingleRelationship(R1, OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);
                        assertEquals(0, count(r2.getPropertyKeys()));

                        assertTrue(transactionData.hasBeenCreated(r1));
                        assertTrue(transactionData.hasBeenCreated(r2));
                        assertFalse(transactionData.hasBeenCreated(db.getNodeById(3).getSingleRelationship(R1, OUTGOING)));

                        //in contrast to filtered version:
                        assertTrue(r2.getEndNode().hasProperty(PLACE));
                        assertNotNull(r2.getEndNode().getSingleRelationship(R3, OUTGOING));
                    }
                }
        );
    }

    @Test
    public void startingWithCreatedRelationshipCurrentGraphVersionShouldBeTraversed() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> created = propertyContainersToMap(transactionData.getAllCreatedRelationships());

                        long r2Id = db.getNodeById(1).getSingleRelationship(R1, OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);

                        assertEquals("NewOne", r2.getStartNode().getProperty(NAME));
                        assertFalse(r2.getStartNode().hasProperty(COUNT));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) r2.getStartNode().getProperty(TAGS)));

                        assertNull(r2.getEndNode().getSingleRelationship(R3, INCOMING));
                        assertEquals(7, r2.getEndNode().getSingleRelationship(R2, INCOMING).getStartNode().getId());
                    }
                }
        );
    }

    @Test
    public void changedRelationshipsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        assertEquals(1, changed.size());

                        Relationship previous = changed.entrySet().iterator().next().getValue().getPrevious();
                        assertEquals(2, count(previous.getPropertyKeys()));
                        assertEquals(3, previous.getProperty(TIME));
                        assertEquals("cool", previous.getProperty(TAG));

                        Relationship current = changed.entrySet().iterator().next().getValue().getCurrent();
                        assertEquals(2, count(current.getPropertyKeys()));
                        assertEquals(4, current.getProperty(TIME));
                        assertEquals("cool", current.getProperty(TAGS));

                        assertTrue(transactionData.hasBeenChanged(previous));
                        assertTrue(transactionData.hasBeenChanged(current));
                        assertFalse(transactionData.hasBeenChanged(db.getNodeById(3).getSingleRelationship(R1, OUTGOING)));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedRelationshipPreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship previous = changed.entrySet().iterator().next().getValue().getPrevious();

                        assertEquals("One", previous.getEndNode().getProperty(NAME));
                        assertEquals(1, previous.getEndNode().getProperty(COUNT, 2));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) previous.getEndNode().getProperty(TAGS)));
                        assertEquals(3, count(previous.getEndNode().getPropertyKeys()));

                        assertEquals("Three", previous.getStartNode().getProperty(NAME));
                        assertEquals("London", previous.getStartNode().getProperty(PLACE));
                        assertEquals("nothing", previous.getStartNode().getProperty(TAGS, "nothing"));

                        Node endNode = previous.getEndNode();
                        Relationship r1 = endNode.getSingleRelationship(R1, OUTGOING);
                        Node endNode1 = r1.getEndNode();
                        assertEquals("Two", endNode1.getProperty(NAME));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedRelationshipPreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship previous = changed.entrySet().iterator().next().getValue().getPrevious();

                        TraversalDescription traversalDescription = db.traversalDescription()
                                .relationships(R1, OUTGOING)
                                .relationships(R2, OUTGOING)
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
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship current = changed.entrySet().iterator().next().getValue().getCurrent();

                        current = transactionData.getChanged(current).getCurrent();

                        assertEquals("NewOne", current.getEndNode().getProperty(NAME));
                        assertEquals(2, current.getEndNode().getProperty(COUNT, 2));
                        assertFalse(current.getEndNode().hasProperty(COUNT));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) current.getEndNode().getProperty(TAGS)));
                        assertEquals(2, count(current.getEndNode().getPropertyKeys()));

                        assertEquals("Three", current.getStartNode().getProperty(NAME));
                        assertEquals("London", current.getStartNode().getProperty(PLACE));
                        assertEquals("one", current.getStartNode().getProperty(TAGS));

                        assertEquals("Three", current.getEndNode().getSingleRelationship(R1, OUTGOING).getEndNode().getProperty(NAME));
                    }
                }
        );
    }

    @Test
    public void startingWithCurrentChangedRelationshipCurrentGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship current = changed.entrySet().iterator().next().getValue().getCurrent();

                        TraversalDescription traversalDescription = db.traversalDescription()
                                .relationships(R1, OUTGOING)
                                .relationships(R2, OUTGOING)
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
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> deleted = propertyContainersToMap(transactionData.getAllDeletedRelationships());
                        assertEquals(4, deleted.size());

                        long r1Id = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getSingleRelationship(R1, INCOMING).getId();
                        Relationship r1 = deleted.get(r1Id);
                        assertEquals(1, r1.getProperty(TIME));
                        assertEquals(1, count(r1.getPropertyKeys()));

                        long r2Id = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getSingleRelationship(R2, INCOMING).getId();
                        Relationship r2 = deleted.get(r2Id);
                        assertEquals(0, count(r2.getPropertyKeys()));

                        Iterator<Relationship> relationships = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getRelationships(R2, OUTGOING).iterator();
                        long r3Id = relationships.next().getId();
                        if (r3Id == r2Id) {
                            r3Id = relationships.next().getId();
                        }
                        Relationship r3 = deleted.get(r3Id);
                        assertEquals(2, r3.getProperty(TIME));
                        assertEquals(1, count(r3.getPropertyKeys()));

                        long r4Id = changesToMap(transactionData.getAllChangedNodes()).get(3L).getPrevious().getSingleRelationship(R3, INCOMING).getId();
                        Relationship r4 = deleted.get(r4Id);
                        assertEquals(0, count(r4.getPropertyKeys()));

                        assertTrue(transactionData.hasBeenDeleted(r1));
                        assertTrue(transactionData.hasBeenDeleted(r2));
                        assertTrue(transactionData.hasBeenDeleted(r3));
                        assertTrue(transactionData.hasBeenDeleted(r4));
                        assertFalse(transactionData.hasBeenDeleted(db.getNodeById(3).getSingleRelationship(R3, OUTGOING)));

                        assertEquals(3, count(transactionData.getDeletedRelationships(db.getNodeById(2))));
                        assertEquals(3, count(transactionData.getDeletedRelationships(db.getNodeById(2), R2, R1)));
                        assertEquals(2, count(transactionData.getDeletedRelationships(db.getNodeById(2), R2)));
                        assertEquals(2, count(transactionData.getDeletedRelationships(db.getNodeById(2), OUTGOING)));
                        assertEquals(2, count(transactionData.getDeletedRelationships(db.getNodeById(2), OUTGOING, R2)));
                        assertEquals(1, count(transactionData.getDeletedRelationships(db.getNodeById(2), INCOMING, R2)));
                        assertEquals(0, count(transactionData.getDeletedRelationships(db.getNodeById(2), R3)));
                    }
                }
        );
    }

    @Test
    public void startingWithDeletedRelationshipPreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> deleted = propertyContainersToMap(transactionData.getAllDeletedRelationships());
                        long r4Id = changesToMap(transactionData.getAllChangedNodes()).get(3L).getPrevious().getSingleRelationship(R3, INCOMING).getId();
                        Relationship r4 = deleted.get(r4Id);

                        Relationship deletedRel = transactionData.getDeleted(r4);

                        assertEquals("One", deletedRel.getStartNode().getProperty(NAME));
                        assertEquals(1, deletedRel.getStartNode().getProperty(COUNT, 2));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) deletedRel.getStartNode().getProperty(TAGS)));
                        assertEquals(3, count(deletedRel.getStartNode().getPropertyKeys()));

                        assertEquals("Three", deletedRel.getEndNode().getProperty(NAME));
                        assertEquals("London", deletedRel.getEndNode().getProperty(PLACE));
                        assertEquals("nothing", deletedRel.getEndNode().getProperty(TAGS, "nothing"));

                        Node startNode = deletedRel.getStartNode();
                        Relationship r5 = startNode.getSingleRelationship(R1, OUTGOING);
                        assertEquals("Two", r5.getEndNode().getProperty(NAME));

                        assertEquals(4, count(startNode.getRelationships()));
                        assertEquals(3, count(startNode.getRelationships(OUTGOING)));
                        assertEquals(2, count(startNode.getRelationships(R3)));
                        assertEquals(3, count(startNode.getRelationships(R3, R1)));
                        assertEquals(1, count(startNode.getRelationships(INCOMING, R3, R1)));
                    }
                }
        );
    }

    @Test
    public void startingWithDeletedRelationshipPreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Relationship> deleted = propertyContainersToMap(transactionData.getAllDeletedRelationships());
                        long r4Id = changesToMap(transactionData.getAllChangedNodes()).get(3L).getPrevious().getSingleRelationship(R3, INCOMING).getId();
                        Relationship deletedRel = transactionData.getDeleted(deleted.get(r4Id));

                        TraversalDescription traversalDescription = db.traversalDescription()
                                .relationships(R1, OUTGOING)
                                .relationships(R2, OUTGOING)
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
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getAllChangedRelationships().iterator().next();

                        assertTrue(transactionData.hasPropertyBeenCreated(change.getCurrent(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenCreated(change.getCurrent(), TIME));
                        assertTrue(transactionData.hasPropertyBeenCreated(change.getPrevious(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenCreated(change.getPrevious(), TAG));

                        assertEquals(1, transactionData.createdProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.createdProperties(change.getPrevious()).size());
                        assertEquals("cool", transactionData.createdProperties(change.getCurrent()).get(TAGS));
                        assertEquals("cool", transactionData.createdProperties(change.getPrevious()).get(TAGS));

                        assertFalse(transactionData.hasPropertyBeenCreated(transactionData.getAllDeletedRelationships().iterator().next(), TAGS));

                        //created relationship should not fall into this category
                        assertFalse(transactionData.hasPropertyBeenCreated(db.getNodeById(7).getSingleRelationship(R2, OUTGOING), TIME));
                    }
                }
        );
    }

    @Test
    public void changedRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getAllChangedRelationships().iterator().next();

                        assertTrue(transactionData.hasPropertyBeenChanged(change.getCurrent(), TIME));
                        assertFalse(transactionData.hasPropertyBeenChanged(change.getCurrent(), TAGS));
                        assertTrue(transactionData.hasPropertyBeenChanged(change.getPrevious(), TIME));
                        assertFalse(transactionData.hasPropertyBeenChanged(change.getPrevious(), TAG));

                        assertEquals(1, transactionData.changedProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.changedProperties(change.getPrevious()).size());
                        assertEquals(3, transactionData.changedProperties(change.getCurrent()).get(TIME).getPrevious());
                        assertEquals(3, transactionData.changedProperties(change.getPrevious()).get(TIME).getPrevious());
                        assertEquals(4, transactionData.changedProperties(change.getCurrent()).get(TIME).getCurrent());
                        assertEquals(4, transactionData.changedProperties(change.getPrevious()).get(TIME).getCurrent());

                        assertFalse(transactionData.hasPropertyBeenChanged(transactionData.getAllDeletedRelationships().iterator().next(), TAGS));
                    }
                }
        );
    }

    @Test
    public void deletedRelationshipPropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Relationship> change = transactionData.getAllChangedRelationships().iterator().next();

                        assertTrue(transactionData.hasPropertyBeenDeleted(change.getCurrent(), TAG));
                        assertFalse(transactionData.hasPropertyBeenDeleted(change.getCurrent(), TIME));
                        assertTrue(transactionData.hasPropertyBeenDeleted(change.getPrevious(), TAG));
                        assertFalse(transactionData.hasPropertyBeenDeleted(change.getPrevious(), TAGS));

                        assertEquals(1, transactionData.deletedProperties(change.getCurrent()).size());
                        assertEquals(1, transactionData.deletedProperties(change.getPrevious()).size());
                        assertEquals("cool", transactionData.deletedProperties(change.getCurrent()).get(TAG));
                        assertEquals("cool", transactionData.deletedProperties(change.getPrevious()).get(TAG));

                        assertFalse(transactionData.hasPropertyBeenDeleted(transactionData.getAllCreatedRelationships().iterator().next(), TAGS));

                        //deleted relationships' props don't qualify
                        Iterator<Relationship> iterator = transactionData.getAllDeletedRelationships().iterator();
                        assertFalse(transactionData.hasPropertyBeenDeleted(iterator.next(), TIME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(iterator.next(), TIME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(iterator.next(), TIME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(iterator.next(), TIME));
                    }
                }
        );
    }

    @Test
    public void createdNodesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> createdNodes = propertyContainersToMap(transactionData.getAllCreatedNodes());
                        assertEquals(1, createdNodes.size());

                        Node createdNode = createdNodes.get(7L);
                        assertEquals("Seven", createdNode.getProperty(NAME));
//                        assertEquals("SomeLabel", createdNode.getLabels().iterator().next().name());
                        assertEquals(4L, createdNode.getProperty("size"));
                        assertEquals(2, count(createdNode.getPropertyKeys()));

                        assertTrue(transactionData.hasBeenCreated(createdNode));
                        assertFalse(transactionData.hasBeenCreated(db.getNodeById(3)));
                    }
                }
        );
    }

    @Test
    public void startingWithCreatedNodeCurrentGraphVersionShouldBeTraversed() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> createdNodes = propertyContainersToMap(transactionData.getAllCreatedNodes());
                        Node createdNode = createdNodes.get(7L);

                        assertEquals("one", createdNode.getSingleRelationship(R2, OUTGOING).getEndNode().getProperty(TAGS));
                        assertFalse(createdNode.getSingleRelationship(R2, OUTGOING).getEndNode().getRelationships(R3, INCOMING).iterator().hasNext());
                    }
                }
        );
    }

    @Test
    public void changedNodesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());
                        assertEquals(4, changed.size());

                        Node previous1 = changed.get(1L).getPrevious();
                        assertEquals(3, count(previous1.getPropertyKeys()));
                        assertEquals("One", previous1.getProperty(NAME));
                        assertEquals(1, previous1.getProperty(COUNT));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) previous1.getProperty(TAGS)));

                        Node current1 = changed.get(1L).getCurrent();
                        assertEquals(2, count(current1.getPropertyKeys()));
                        assertEquals("NewOne", current1.getProperty(NAME));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) current1.getProperty(TAGS)));

                        Node previous2 = changed.get(3L).getPrevious();
                        assertEquals(2, count(previous2.getPropertyKeys()));
                        assertEquals("Three", previous2.getProperty(NAME));
                        assertEquals("London", previous2.getProperty(PLACE));

                        Node current2 = changed.get(3L).getCurrent();
                        assertEquals(3, count(current2.getPropertyKeys()));
                        assertEquals("Three", current2.getProperty(NAME));
                        assertEquals("London", current2.getProperty(PLACE));
                        assertEquals("one", current2.getProperty(TAGS));

                        Node previous3 = changed.get(5L).getPrevious();
                        assertEquals("Five", previous3.getProperty(NAME));
                        assertEquals(1, count(previous3.getLabels()));
                        assertEquals("SomeLabel", previous3.getLabels().iterator().next().name());

                        Node current3 = changed.get(5L).getCurrent();
                        assertEquals("Five", current3.getProperty(NAME));
                        assertEquals(2, count(current3.getLabels()));
                        assertTrue(contains(current3.getLabels(), label("SomeLabel")));
                        assertTrue(contains(current3.getLabels(), label("NewLabel")));

                        Node previous4 = changed.get(6L).getPrevious();
                        assertEquals("Six", previous4.getProperty(NAME));
                        assertEquals(1, count(previous4.getLabels()));
                        assertEquals("ToBeRemoved", previous4.getLabels().iterator().next().name());

                        Node current4 = changed.get(6L).getCurrent();
                        assertEquals("Six", current4.getProperty(NAME));
                        assertEquals(0, count(current4.getLabels()));

                        assertTrue(transactionData.hasBeenChanged(previous1));
                        assertTrue(transactionData.hasBeenChanged(previous2));
                        assertTrue(transactionData.hasBeenChanged(previous3));
                        assertTrue(transactionData.hasBeenChanged(previous4));
                        assertTrue(transactionData.hasBeenChanged(current1));
                        assertTrue(transactionData.hasBeenChanged(current2));
                        assertTrue(transactionData.hasBeenChanged(current3));
                        assertTrue(transactionData.hasBeenChanged(current4));
                        assertFalse(transactionData.hasBeenChanged(db.getNodeById(4)));
                    }
                }
        );
    }

    @Test
    public void startingWithPreviousChangedNodePreviousGraphVersionShouldBeTraversedUsingNativeApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node current = changed.get(1L).getCurrent();
                        Node previous = transactionData.getChanged(current).getPrevious();

                        assertEquals(1, previous.getSingleRelationship(R1, OUTGOING).getProperty(TIME));
                        assertEquals("Two", previous.getRelationships(R1, OUTGOING).iterator().next().getEndNode().getProperty(NAME));
                        assertEquals("Three", previous.getRelationships(OUTGOING, R3).iterator().next().getEndNode().getProperty(NAME));
                        assertEquals(2L, previous.getRelationships(R1).iterator().next().getEndNode().getProperty("size"));

                        assertNull(previous.getSingleRelationship(R1, INCOMING));
                        assertEquals(4, count(previous.getRelationships()));
                        assertEquals(3, count(previous.getRelationships(R1, R3)));
                        assertEquals(1, count(previous.getRelationships(R1)));
                        assertEquals(3, count(previous.getRelationships(OUTGOING)));
                        assertEquals(1, count(previous.getRelationships(INCOMING)));
                        assertEquals(1, count(previous.getRelationships(OUTGOING, R3)));
                        assertEquals(2, count(previous.getRelationships(OUTGOING, R1, R3)));
                        assertEquals(1, count(previous.getRelationships(R3, OUTGOING)));

                        assertTrue(previous.hasRelationship());
                        assertTrue(previous.hasRelationship(R1, R3));
                        assertTrue(previous.hasRelationship(R1));
                        assertTrue(previous.hasRelationship(OUTGOING));
                        assertTrue(previous.hasRelationship(INCOMING));
                        assertTrue(previous.hasRelationship(OUTGOING, R3));
                        assertTrue(previous.hasRelationship(OUTGOING, R1, R3));
                        assertTrue(previous.hasRelationship(R3, OUTGOING));

                        assertFalse(previous.hasRelationship(R1, INCOMING));
                        assertFalse(previous.hasRelationship(R2));

                        previous.createRelationshipTo(db.getNodeById(4), R3);
                        try {
                            previous.getSingleRelationship(R3, OUTGOING);
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
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node previous = changed.get(1L).getPrevious();

                        TraversalDescription traversalDescription = db.traversalDescription()
                                .relationships(R1, OUTGOING)
                                .relationships(R2, OUTGOING)
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
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());
                        Node current = changed.get(1L).getCurrent();

                        assertEquals("Three", current.getSingleRelationship(R1, OUTGOING).getEndNode().getProperty(NAME));
                        assertEquals("London", current.getRelationships(R1, OUTGOING).iterator().next().getEndNode().getProperty(PLACE));
                        assertEquals("one", current.getRelationships(OUTGOING, R1).iterator().next().getEndNode().getProperty(TAGS));
                    }
                }
        );
    }

    @Test
    public void startingWithCurrentChangedNodeCurrentGraphVersionShouldBeTraversed() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());
                        Node current = changed.get(1L).getCurrent();

                        TraversalDescription traversalDescription = db.traversalDescription()
                                .relationships(R1, OUTGOING)
                                .relationships(R2, OUTGOING)
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
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        assertEquals(1, deletedNodes.size());

                        Node deleted = deletedNodes.get(2L);
                        Node one = transactionData.getDeleted(deleted).getSingleRelationship(R1, INCOMING).getStartNode();

                        assertEquals("Two", deleted.getProperty(NAME));
                        assertEquals(2L, deleted.getProperty("size"));
                        assertEquals(2, count(deleted.getPropertyKeys()));

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
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);

                        Node one = transactionData.getDeleted(deleted).getSingleRelationship(R1, INCOMING).getStartNode();

                        assertEquals(1, one.getSingleRelationship(R1, OUTGOING).getProperty(TIME));
                        assertEquals("Two", one.getRelationships(R1, OUTGOING).iterator().next().getEndNode().getProperty(NAME));
                        assertEquals("Three", one.getRelationships(OUTGOING, R3).iterator().next().getEndNode().getProperty(NAME));
                        assertEquals(2L, one.getRelationships(R1).iterator().next().getEndNode().getProperty("size"));
                    }
                }
        );
    }

    @Test
    public void startingWithDeletedNodePreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);

                        Node one = transactionData.getDeleted(deleted).getSingleRelationship(R1, INCOMING).getStartNode();

                        TraversalDescription traversalDescription = db.traversalDescription()
                                .relationships(R1, OUTGOING)
                                .relationships(R2, OUTGOING)
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
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Node> changed = changesToMap(transactionData.getAllChangedNodes()).get(3L);

                        assertTrue(transactionData.hasPropertyBeenCreated(changed.getCurrent(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenCreated(changed.getCurrent(), NAME));
                        assertTrue(transactionData.hasPropertyBeenCreated(changed.getPrevious(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenCreated(changed.getPrevious(), NAME));

                        assertEquals(1, transactionData.createdProperties(changed.getCurrent()).size());
                        assertEquals(1, transactionData.createdProperties(changed.getPrevious()).size());
                        assertEquals("one", transactionData.createdProperties(changed.getCurrent()).get(TAGS));
                        assertEquals("one", transactionData.createdProperties(changed.getPrevious()).get(TAGS));

                        assertFalse(transactionData.hasPropertyBeenCreated(changesToMap(transactionData.getAllChangedNodes()).get(1L).getCurrent(), TAGS));
                    }
                }
        );
    }

    @Test
    public void changedNodePropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Node> changed = changesToMap(transactionData.getAllChangedNodes()).get(1L);

                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getCurrent(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), COUNT));
                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getPrevious(), NAME));
                        assertTrue(transactionData.hasPropertyBeenChanged(changed.getPrevious(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), COUNT));

                        assertEquals(2, transactionData.changedProperties(changed.getCurrent()).size());
                        assertEquals(2, transactionData.changedProperties(changed.getPrevious()).size());
                        assertEquals("One", transactionData.changedProperties(changed.getCurrent()).get(NAME).getPrevious());
                        assertEquals("One", transactionData.changedProperties(changed.getPrevious()).get(NAME).getPrevious());
                        assertEquals("NewOne", transactionData.changedProperties(changed.getCurrent()).get(NAME).getCurrent());
                        assertEquals("NewOne", transactionData.changedProperties(changed.getPrevious()).get(NAME).getCurrent());
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) transactionData.changedProperties(changed.getCurrent()).get(TAGS).getCurrent()));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) transactionData.changedProperties(changed.getPrevious()).get(TAGS).getPrevious()));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) transactionData.changedProperties(changed.getCurrent()).get(TAGS).getCurrent()));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) transactionData.changedProperties(changed.getPrevious()).get(TAGS).getPrevious()));

                        assertEquals(3, count(changed.getPrevious().getPropertyKeys()));
                        assertEquals(2, count(changed.getCurrent().getPropertyKeys()));

                        changed = changesToMap(transactionData.getAllChangedNodes()).get(3L);
                        assertEquals(0, transactionData.changedProperties(changed.getCurrent()).size());
                        assertEquals(0, transactionData.changedProperties(changed.getPrevious()).size());
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), PLACE));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), NAME));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getPrevious(), PLACE));

                        assertFalse(transactionData.hasPropertyBeenChanged(transactionData.getAllDeletedNodes().iterator().next(), NAME));
                        assertFalse(transactionData.hasPropertyBeenChanged(transactionData.getAllCreatedNodes().iterator().next(), NAME));

                        assertEquals(2, count(changed.getPrevious().getPropertyKeys()));
                        assertEquals(3, count(changed.getCurrent().getPropertyKeys()));

                        //one that isn't changed
                        Node unchanged = changesToMap(transactionData.getAllChangedNodes()).get(1L).getPrevious().getSingleRelationship(R3, OUTGOING).getEndNode().getSingleRelationship(R1, OUTGOING).getEndNode();
                        assertEquals(1, count(unchanged.getPropertyKeys()));
                        assertEquals(NAME, unchanged.getPropertyKeys().iterator().next());
                        assertEquals("Four", unchanged.getProperty(NAME));
                        assertEquals("Four", unchanged.getProperty(NAME, "nothing"));
                        assertEquals("nothing", unchanged.getProperty("non-existing", "nothing"));

                        //labels changed
                        changed = changesToMap(transactionData.getAllChangedNodes()).get(5L);
                        assertEquals(0, transactionData.changedProperties(changed.getCurrent()).size());
                        assertEquals(0, transactionData.changedProperties(changed.getPrevious()).size());
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertEquals(1, transactionData.assignedLabels(changed.getPrevious()).size());
                        assertEquals(1, transactionData.assignedLabels(changed.getCurrent()).size());
                        assertEquals("NewLabel", transactionData.assignedLabels(changed.getPrevious()).iterator().next().name());
                        assertEquals("NewLabel", transactionData.assignedLabels(changed.getCurrent()).iterator().next().name());
                        assertTrue(transactionData.hasLabelBeenAssigned(changed.getPrevious(), label("NewLabel")));
                        assertTrue(transactionData.hasLabelBeenAssigned(changed.getCurrent(), label("NewLabel")));
                        assertFalse(transactionData.hasLabelBeenAssigned(changed.getPrevious(), label("SomeOther")));
                        assertFalse(transactionData.hasLabelBeenAssigned(changed.getCurrent(), label("SomeOther")));
                        assertFalse(transactionData.hasLabelBeenRemoved(changed.getPrevious(), label("NewLabel")));
                        assertFalse(transactionData.hasLabelBeenRemoved(changed.getCurrent(), label("NewLabel")));
                        assertEquals(0, transactionData.removedLabels(changed.getPrevious()).size());
                        assertEquals(0, transactionData.removedLabels(changed.getCurrent()).size());

                        changed = changesToMap(transactionData.getAllChangedNodes()).get(6L);
                        assertEquals(0, transactionData.changedProperties(changed.getCurrent()).size());
                        assertEquals(0, transactionData.changedProperties(changed.getPrevious()).size());
                        assertFalse(transactionData.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertEquals(1, transactionData.removedLabels(changed.getPrevious()).size());
                        assertEquals(1, transactionData.removedLabels(changed.getCurrent()).size());
                        assertEquals("ToBeRemoved", transactionData.removedLabels(changed.getPrevious()).iterator().next().name());
                        assertEquals("ToBeRemoved", transactionData.removedLabels(changed.getCurrent()).iterator().next().name());
                        assertTrue(transactionData.hasLabelBeenRemoved(changed.getPrevious(), label("ToBeRemoved")));
                        assertTrue(transactionData.hasLabelBeenRemoved(changed.getCurrent(), label("ToBeRemoved")));
                        assertFalse(transactionData.hasLabelBeenRemoved(changed.getPrevious(), label("SomeOther")));
                        assertFalse(transactionData.hasLabelBeenRemoved(changed.getCurrent(), label("SomeOther")));
                        assertFalse(transactionData.hasLabelBeenAssigned(changed.getPrevious(), label("NewLabel")));
                        assertFalse(transactionData.hasLabelBeenAssigned(changed.getCurrent(), label("NewLabel")));
                        assertEquals(0, transactionData.assignedLabels(changed.getPrevious()).size());
                        assertEquals(0, transactionData.assignedLabels(changed.getCurrent()).size());
                    }
                }
        );
    }

    @Test
    public void deletedNodePropertiesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        assertTrue(transactionData.mutationsOccurred());

                        Change<Node> changed = changesToMap(transactionData.getAllChangedNodes()).get(1L);

                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), NAME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), TAGS));
                        assertTrue(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), COUNT));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), NAME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), TAGS));
                        assertTrue(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), COUNT));

                        assertEquals(1, transactionData.deletedProperties(changed.getCurrent()).size());
                        assertEquals(1, transactionData.deletedProperties(changed.getPrevious()).size());
                        assertEquals(1, transactionData.deletedProperties(changed.getCurrent()).get(COUNT));
                        assertEquals(1, transactionData.deletedProperties(changed.getPrevious()).get(COUNT));

                        changed = changesToMap(transactionData.getAllChangedNodes()).get(3L);
                        assertEquals(0, transactionData.deletedProperties(changed.getCurrent()).size());
                        assertEquals(0, transactionData.deletedProperties(changed.getPrevious()).size());
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), NAME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getCurrent(), PLACE));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), NAME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), TAGS));
                        assertFalse(transactionData.hasPropertyBeenDeleted(changed.getPrevious(), PLACE));

                        assertFalse(transactionData.hasPropertyBeenDeleted(transactionData.getAllDeletedNodes().iterator().next(), NAME));
                        assertFalse(transactionData.hasPropertyBeenDeleted(transactionData.getAllCreatedNodes().iterator().next(), NAME));
                    }
                }
        );
    }

    //mutations

    @Test
    public void shouldBeAbleToChangeCreatedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Relationship> created = propertyContainersToMap(transactionData.getAllCreatedRelationships());

                        long r1Id = db.getNodeById(7).getSingleRelationship(R2, OUTGOING).getId();
                        Relationship r1 = created.get(r1Id);

                        r1.setProperty("additional", "someValue");
                        r1.removeProperty(TIME);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {
            Relationship r1 = db.getNodeById(7).getSingleRelationship(R2, OUTGOING);
            assertEquals(1, count(r1.getPropertyKeys()));
            assertEquals("someValue", r1.getProperty("additional"));
            assertFalse(r1.hasProperty(TIME));
        }
    }

    @Test
    public void shouldBeAbleToChangeCreatedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Node> createdNodes = propertyContainersToMap(transactionData.getAllCreatedNodes());
                        Node createdNode = createdNodes.get(7L);

                        createdNode.setProperty(NAME, "NewSeven");
                        createdNode.setProperty("additional", "something");
                        createdNode.removeProperty("size");
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {

            Node createdNode = db.getNodeById(7L);

            assertEquals("NewSeven", createdNode.getProperty(NAME));
            assertEquals("something", createdNode.getProperty("additional"));
            assertEquals(2, count(createdNode.getPropertyKeys()));
            assertFalse(createdNode.hasProperty("size"));
        }
    }

    @Test
    public void shouldBeAbleToChangeCurrentChangedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());

                        Relationship r = changed.entrySet().iterator().next().getValue().getCurrent();

                        r.setProperty(TIME, 5);
                        r.setProperty("additional", "something");
                        r.removeProperty(TAGS);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {
            Relationship r = db.getNodeById(3).getSingleRelationship(R3, OUTGOING);

            assertEquals(2, count(r.getPropertyKeys()));
            assertEquals(5, r.getProperty(TIME));
            assertEquals("something", r.getProperty("additional"));
            assertFalse(r.hasProperty(TAGS));
        }
    }

    @Test
    public void shouldBeAbleToChangePreviousChangedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());

                        Relationship r = changed.entrySet().iterator().next().getValue().getPrevious();

                        r.setProperty(TIME, 5);
                        r.setProperty("additional", "something");
                        r.removeProperty(TAGS);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {

            Relationship r = db.getNodeById(3).getSingleRelationship(R3, OUTGOING);

            assertEquals(2, count(r.getPropertyKeys()));
            assertEquals(5, r.getProperty(TIME));
            assertEquals("something", r.getProperty("additional"));
            assertFalse(r.hasProperty(TAGS));
        }
    }

    @Test
    public void shouldBeAbleToChangeCurrentChangedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        node.setProperty(NAME, "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty(TAGS);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {

            Node node = db.getNodeById(1L);

            assertEquals(2, count(node.getPropertyKeys()));
            assertEquals("YetAnotherOne", node.getProperty(NAME));
            assertEquals("something", node.getProperty("additional"));
            assertFalse(node.hasProperty(TAGS));
        }
    }

    @Test
    public void shouldBeAbleToChangePreviousChangedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        node.setProperty(NAME, "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty(TAGS);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {

            Node node = db.getNodeById(1L);

            assertEquals(2, count(node.getPropertyKeys()));
            assertEquals("YetAnotherOne", node.getProperty(NAME));
            assertEquals("something", node.getProperty("additional"));
            assertFalse(node.hasProperty(TAGS));
        }
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToChangeDeletedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Relationship> deleted = propertyContainersToMap(transactionData.getAllDeletedRelationships());

                        long r1Id = propertyContainersToMap(transactionData.getAllDeletedNodes()).get(2L).getSingleRelationship(R1, INCOMING).getId();
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
                new BeforeCommitCallback.RememberingAdapter() {
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
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());

                        Node deleted = deletedNodes.get(2L);

                        deleted.createRelationshipTo(db.getNodeById(3), withName("illegal"));
                    }
                }
        );
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToCreateARelationshipToDeletedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());

                        Node deleted = deletedNodes.get(2L);

                        db.getNodeById(3).createRelationshipTo(deleted, withName("illegal"));
                    }
                }
        );
    }

    @Test
    public void shouldChangeNothingIfTxRollsBack() {
        createTestDatabase();
        mutateGraph(
                new TestGraphMutation(),
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        throw new RuntimeException("Deliberate testing exception");
                    }
                },
                KeepCalmAndCarryOn.getInstance()
        );

        try (Transaction tx = db.beginTx()) {

            long r4Id = db.getNodeById(3L).getSingleRelationship(R3, INCOMING).getId();
            Relationship r4 = db.getRelationshipById(r4Id);

            assertEquals("One", r4.getStartNode().getProperty(NAME));
            assertEquals(1, r4.getStartNode().getProperty(COUNT, 2));
            assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) r4.getStartNode().getProperty(TAGS)));
            assertEquals(3, count(r4.getStartNode().getPropertyKeys()));

            assertEquals("Three", r4.getEndNode().getProperty(NAME));
            assertEquals("London", r4.getEndNode().getProperty(PLACE));
            assertEquals("nothing", r4.getEndNode().getProperty(TAGS, "nothing"));

            Node startNode = r4.getStartNode();
            Relationship r5 = startNode.getSingleRelationship(R1, OUTGOING);
            assertEquals("Two", r5.getEndNode().getProperty(NAME));

            assertEquals(4, count(startNode.getRelationships()));
            assertEquals(3, count(startNode.getRelationships(OUTGOING)));
            assertEquals(2, count(startNode.getRelationships(R3)));
            assertEquals(3, count(startNode.getRelationships(R3, R1)));
            assertEquals(1, count(startNode.getRelationships(INCOMING, R3, R1)));
        }
    }

    @Test
    public void shouldBeAbleToDeleteChangedNodeCommittingTransaction() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Change<Node> change = transactionData.getChanged(db.getNodeById(1));
                        //must first delete the new relationship
                        change.getCurrent().getSingleRelationship(R1, OUTGOING).delete();
                        deleteNodeAndRelationships(change.getPrevious());
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {
            assertEquals(6, countNodes(db));
        }
    }

    @Test
    public void shouldBeAbleToWipeTheGraphBeforeCommittingTransaction() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        for (Node node : GlobalGraphOperations.at(db).getAllNodes()) {
                            deleteNodeAndRelationships(node);
                        }
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {
            assertEquals(0, countNodes(db));
        }
    }

    @Test
    public void shouldNotChangeAnythingWhenDeletingAlreadyDeletedNodeAndRelationships() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Map<Long, Node> deletedNodes = propertyContainersToMap(transactionData.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);
                        deleteNodeAndRelationships(deleted);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {
            assertEquals(7, countNodes(db));
        }
    }

    @Test
    public void shouldBeAbleToCreateAdditionalNodesAndRelationshipsFromCurrentGraphVersion() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        Node newNode = db.createNode();
                        newNode.setProperty(NAME, "Eight");
                        node.createRelationshipTo(newNode, withName("R4")).setProperty("new", true);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {
            Relationship newRelationship = db.getNodeById(1).getSingleRelationship(withName("R4"), OUTGOING);
            assertNotNull(newRelationship);
            assertEquals("Eight", newRelationship.getEndNode().getProperty(NAME));
            assertEquals(true, newRelationship.getProperty("new"));
        }
    }

    @Test
    public void shouldBeAbleToCreateAdditionalNodesAndRelationshipsFromPreviousGraphVersion() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        if (!transactionData.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(transactionData.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        Node newNode = db.createNode();
                        newNode.setProperty(NAME, "Eight");
                        node.createRelationshipTo(newNode, withName("R4")).setProperty("new", true);
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {
            Relationship newRelationship = db.getNodeById(1).getSingleRelationship(withName("R4"), OUTGOING);
            assertNotNull(newRelationship);
            assertEquals("Eight", newRelationship.getEndNode().getProperty(NAME));
            assertEquals(true, newRelationship.getProperty("new"));
        }
    }

    @Test
    public void propertyExtractionStrategySmokeTest() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData transactionData) {
                        Relationship previous = transactionData.getAllChangedRelationships().iterator().next().getPrevious();
                        Relationship current = transactionData.getAllChangedRelationships().iterator().next().getCurrent();

                        Map<String, Object> previousProps = new OtherNodeNameIncludingRelationshipPropertiesExtractor().extractProperties(previous, previous.getStartNode());
                        assertEquals(3, previousProps.size());
                        assertEquals("One", previousProps.get("otherNodeName"));
                        assertEquals(3, previousProps.get(TIME));
                        assertEquals("cool", previousProps.get(TAG));

                        Map<String, Object> currentProps = new OtherNodeNameIncludingRelationshipPropertiesExtractor().extractProperties(current, current.getStartNode());
                        assertEquals(3, currentProps.size());
                        assertEquals("NewOne", currentProps.get("otherNodeName"));
                        assertEquals(4, currentProps.get(TIME));
                        assertEquals("cool", currentProps.get(TAGS));
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
                            four.setProperty(NAME, "Three");
                            four.setProperty(NAME, "Four");
                        }
                    }, new BeforeCommitCallback() {
                        @Override
                        public void beforeCommit(ImprovedTransactionData transactionData) {
                            assertFalse(transactionData.mutationsOccurred());
                        }

                        @Override
                        public boolean mutationsOccurred() {
                            return true;
                        }
                    }
        );
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
        db.registerTransactionEventHandler(handler);
        new SimpleTransactionExecutor(db).executeInTransaction(transactionCallback, exceptionHandlingStrategy);
        assertTrue(beforeCommitCallback.mutationsOccurred());
    }

    private class TestingTxEventHandler implements TransactionEventHandler {

        private final BeforeCommitCallback beforeCommitCallback;

        private TestingTxEventHandler(BeforeCommitCallback beforeCommitCallback) {
            this.beforeCommitCallback = beforeCommitCallback;
        }

        @Override
        public Object beforeCommit(TransactionData data) throws Exception {
            beforeCommitCallback.beforeCommit(new LazyTransactionData(data));
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
        void beforeCommit(ImprovedTransactionData transactionData);

        boolean mutationsOccurred();

        public abstract class RememberingAdapter implements BeforeCommitCallback {

            private boolean mutationsOccurred = false;

            @Override
            public void beforeCommit(ImprovedTransactionData transactionData) {
                if (!transactionData.mutationsOccurred()) {
                    return;
                }

                mutationsOccurred = true;

                doBeforeCommit(transactionData);

            }

            protected abstract void doBeforeCommit(ImprovedTransactionData transactionData);

            public boolean mutationsOccurred() {
                return mutationsOccurred;
            }
        }
    }

    private class OtherNodeNameIncludingRelationshipPropertiesExtractor {

        public Map<String, Object> extractProperties(Relationship relationship, Node pointOfView) {
            Map<String, Object> result = new HashMap<>();
            result.putAll(propertiesToMap(relationship));
            result.put("otherNodeName", relationship.getOtherNode(pointOfView).getProperty(NAME).toString());
            return result;
        }
    }

    private void createTestDatabase() {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();

        new TestDataBuilder(db)
                .node(label("TestLabel"))
                .node().setProp(NAME, "One").setProp(COUNT, 1).setProp(TAGS, new String[]{"one", "two"})

                .node().setProp(NAME, "Two").setProp("size", 2L)
                .relationshipFrom(1, "R1").setProp(TIME, 1)
                .relationshipFrom(2, "R2")

                .node().setProp(NAME, "Three").setProp(PLACE, "London")
                .relationshipFrom(2, "R2").setProp(TIME, 2)
                .relationshipTo(1, "R3").setProp(TIME, 3).setProp(TAG, "cool")
                .relationshipFrom(1, "R3")

                .node().setProp(NAME, "Four")
                .relationshipFrom(3, "R1").setProp(TIME, 1)
                .relationshipFrom(1, "WHATEVER")

                .node(label("SomeLabel")).setProp(NAME, "Five")
                .node(label("ToBeRemoved")).setProp(NAME, "Six");
    }

    private class TestGraphMutation extends VoidReturningCallback {

        @Override
        public void doInTx(GraphDatabaseService database) {
            Node one = database.getNodeById(1);
            one.setProperty(NAME, "NewOne");
            one.removeProperty(COUNT);
            one.setProperty(TAGS, new String[]{"one"});
            one.setProperty(TAGS, new String[]{"one", "three"});

            Node two = database.getNodeById(2);
            deleteNodeAndRelationships(two);

            Node three = database.getNodeById(3);
            three.setProperty(TAGS, "one");
            three.setProperty(PLACE, "Rome");
            three.setProperty(PLACE, "London");

            Node seven = database.createNode(label("SomeLabel"));
            seven.setProperty(NAME, "Seven");
            seven.setProperty("size", 3L);
            seven.setProperty("size", 4L);
            Relationship r = seven.createRelationshipTo(three, R2);
            r.setProperty(TIME, 4);

            r = three.getSingleRelationship(R3, OUTGOING);
            r.setProperty(TIME, 4);
            r.removeProperty(TAG);
            r.setProperty(TAGS, "cool");

            three.getSingleRelationship(R3, INCOMING).delete();

            one.createRelationshipTo(three, R1);

            //change that should not be picked up as a change
            Node four = database.getNodeById(4);
            four.setProperty(NAME, "Three");
            four.setProperty(NAME, "Four");

            Node five = database.getNodeById(5);
            five.addLabel(label("NewLabel"));
//
            Node six = database.getNodeById(6);
            six.removeLabel(label("ToBeRemoved"));
        }
    }
}
