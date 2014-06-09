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

import java.util.*;

import static com.graphaware.common.util.IterableUtils.*;
import static com.graphaware.common.util.PropertyContainerUtils.*;
import static com.graphaware.tx.event.improved.LazyTransactionDataComprehensiveTest.RelationshipTypes.*;
import static com.graphaware.tx.event.improved.api.Change.changesToMap;
import static junit.framework.Assert.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicLabel.label;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Comprehensive unit test for {@link com.graphaware.tx.event.improved.api.LazyTransactionData}.
 * <p/>
 * Start graph:
 * <p/>
 * CREATE
 * (nodeWithIdZero:TestLabel),
 * (one:One {name:"One", count:1, tags:["one","two"]}),
 * (two {name:"Two", size:2}),
 * (three {name:"Three", place:"London"}),
 * (four {name:"Four"}),
 * (five:SomeLabel {name:"Five"}),
 * (six:ToBeRemoved {name:"Six"}),
 * (one)-[:R1 {time:1}]->(two),
 * (two)-[:R2]->(two),
 * (two)-[:R2 {time:2}]->(three),
 * (three)-[:R3 {time:3, tag:"cool"}]->(one),
 * (one)-[:R3]->(three),
 * (three)-[:R1 {time:1}]->(four),
 * (one)-[:WHATEVER]->(four),
 * (four)-[:R4]->(five),
 * (five)-[:R4]->(six);
 * <p/>
 * Graph after mutation:
 * <p/>
 * CREATE
 * (nodeWithIdZero:TestLabel),
 * (one:NewOne {name:"NewOne", tags:["one","three"]}),
 * (three {name:"Three", place:"London", tags:"one"}),
 * (four {name:"Four"}),
 * (five:SomeLabel:NewLabel {name:"Five"}),
 * (six {name:"Six"}),
 * (seven:SomeLabel {size:4})
 * (three)-[:R3 {time:4, tags:"cool"}]->(one),
 * (three)-[:R1 {time:1}]->(four),
 * (one)-[:R1]->(three)
 * (one)-[:WHATEVER]->(four),
 * (seven)-[:R2 {time:4}]->(three)
 * (four)-[:R4]->(five),
 * (five)-[:R4]->(six);
 */
@SuppressWarnings("deprecation")
public class LazyTransactionDataComprehensiveTest {

    public static final String TIME = "time";
    public static final String PLACE = "place";
    public static final String NAME = "name";
    public static final String COUNT = "count";
    public static final String TAGS = "tags";
    public static final String TAG = "tag";

    public static enum RelationshipTypes implements RelationshipType {
        R1, R2, R3, R4
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Relationship> created = toMap(td.getAllCreatedRelationships());
                        assertEquals(2, created.size());

                        long r1Id = db.getNodeById(7).getSingleRelationship(R2, OUTGOING).getId();
                        Relationship r1 = created.get(r1Id);
                        assertEquals(4, r1.getProperty(TIME));
                        assertEquals(1, count(r1.getPropertyKeys()));

                        long r2Id = db.getNodeById(1).getSingleRelationship(R1, OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);
                        assertEquals(0, count(r2.getPropertyKeys()));

                        assertTrue(td.hasBeenCreated(r1));
                        assertTrue(td.hasBeenCreated(r2));
                        assertFalse(td.hasBeenCreated(db.getNodeById(3).getSingleRelationship(R1, OUTGOING)));

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Relationship> created = toMap(td.getAllCreatedRelationships());

                        long r2Id = db.getNodeById(1).getSingleRelationship(R1, OUTGOING).getId();
                        Relationship r2 = created.get(r2Id);

                        Node one = r2.getStartNode();
                        assertEquals("NewOne", one.getProperty(NAME));
                        assertFalse(one.hasProperty(COUNT));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) one.getProperty(TAGS)));
                        assertEquals(1, count(one.getLabels()));
                        assertTrue(contains(one.getLabels(), label("NewOne")));
                        assertTrue(one.hasLabel(label("NewOne")));
                        assertFalse(one.hasLabel(label("One")));

                        Node three = r2.getEndNode();
                        assertNull(three.getSingleRelationship(R3, INCOMING));
                        assertEquals(7, three.getSingleRelationship(R2, INCOMING).getStartNode().getId());
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Relationship>> changed = changesToMap(td.getAllChangedRelationships());
                        assertEquals(1, changed.size());

                        Relationship previous = getSingleValue(changed).getPrevious();
                        assertEquals(2, count(previous.getPropertyKeys()));
                        assertEquals(3, previous.getProperty(TIME));
                        assertEquals("cool", previous.getProperty(TAG));

                        Relationship current = getSingleValue(changed).getCurrent();
                        assertEquals(2, count(current.getPropertyKeys()));
                        assertEquals(4, current.getProperty(TIME));
                        assertEquals("cool", current.getProperty(TAGS));

                        assertTrue(td.hasBeenChanged(previous));
                        assertTrue(td.hasBeenChanged(current));
                        assertFalse(td.hasBeenChanged(db.getNodeById(3).getSingleRelationship(R1, OUTGOING)));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Relationship>> changed = changesToMap(td.getAllChangedRelationships());
                        Relationship previous = getSingleValue(changed).getPrevious();

                        Node one = previous.getEndNode();
                        assertEquals("One", one.getProperty(NAME));
                        assertEquals(1, one.getProperty(COUNT, 2));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) one.getProperty(TAGS)));
                        assertEquals(3, count(one.getPropertyKeys()));
                        assertEquals(1, count(one.getLabels()));
                        assertTrue(contains(one.getLabels(), label("One")));
                        assertTrue(one.hasLabel(label("One")));
                        assertFalse(one.hasLabel(label("NewOne")));

                        Node three = previous.getStartNode();
                        assertEquals("Three", three.getProperty(NAME));
                        assertEquals("London", three.getProperty(PLACE));
                        assertEquals("nothing", three.getProperty(TAGS, "nothing"));

                        Relationship r1 = one.getSingleRelationship(R1, OUTGOING);
                        assertEquals("Two", r1.getEndNode().getProperty(NAME));
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
                        Map<Long, Change<Relationship>> changed = changesToMap(transactionData.getAllChangedRelationships());
                        Relationship previous = getSingleValue(changed).getPrevious();

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Relationship>> changed = changesToMap(td.getAllChangedRelationships());
                        Relationship current = getSingleValue(changed).getCurrent();

                        current = td.getChanged(current).getCurrent();

                        Node one = current.getEndNode();
                        assertEquals("NewOne", one.getProperty(NAME));
                        assertEquals(2, one.getProperty(COUNT, 2));
                        assertFalse(one.hasProperty(COUNT));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) one.getProperty(TAGS)));
                        assertEquals(2, count(one.getPropertyKeys()));
                        assertEquals(1, count(one.getLabels()));
                        assertTrue(contains(one.getLabels(), label("NewOne")));

                        Node three = current.getStartNode();
                        assertEquals("Three", three.getProperty(NAME));
                        assertEquals("London", three.getProperty(PLACE));
                        assertEquals("one", three.getProperty(TAGS));

                        assertEquals("Three", one.getSingleRelationship(R1, OUTGOING).getEndNode().getProperty(NAME));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Relationship>> changed = changesToMap(td.getAllChangedRelationships());
                        Relationship current = getSingleValue(changed).getCurrent();

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Relationship> deleted = toMap(td.getAllDeletedRelationships());
                        assertEquals(4, deleted.size());

                        long r1Id = toMap(td.getAllDeletedNodes()).get(2L).getSingleRelationship(R1, INCOMING).getId();
                        Relationship r1 = deleted.get(r1Id);
                        assertEquals(1, r1.getProperty(TIME));
                        assertEquals(1, count(r1.getPropertyKeys()));

                        long r2Id = toMap(td.getAllDeletedNodes()).get(2L).getSingleRelationship(R2, INCOMING).getId();
                        Relationship r2 = deleted.get(r2Id);
                        assertEquals(0, count(r2.getPropertyKeys()));

                        Iterator<Relationship> relationships = toMap(td.getAllDeletedNodes()).get(2L).getRelationships(R2, OUTGOING).iterator();
                        long r3Id = relationships.next().getId();
                        if (r3Id == r2Id) {
                            r3Id = relationships.next().getId();
                        }
                        Relationship r3 = deleted.get(r3Id);
                        assertEquals(2, r3.getProperty(TIME));
                        assertEquals(1, count(r3.getPropertyKeys()));

                        long r4Id = changesToMap(td.getAllChangedNodes()).get(3L).getPrevious().getSingleRelationship(R3, INCOMING).getId();
                        Relationship r4 = deleted.get(r4Id);
                        assertEquals(0, count(r4.getPropertyKeys()));

                        assertTrue(td.hasBeenDeleted(r1));
                        assertTrue(td.hasBeenDeleted(r2));
                        assertTrue(td.hasBeenDeleted(r3));
                        assertTrue(td.hasBeenDeleted(r4));
                        assertFalse(td.hasBeenDeleted(db.getNodeById(3).getSingleRelationship(R3, OUTGOING)));

                        assertEquals(3, count(td.getDeletedRelationships(db.getNodeById(2))));
                        assertEquals(3, count(td.getDeletedRelationships(db.getNodeById(2), R2, R1)));
                        assertEquals(2, count(td.getDeletedRelationships(db.getNodeById(2), R2)));
                        assertEquals(2, count(td.getDeletedRelationships(db.getNodeById(2), OUTGOING)));
                        assertEquals(2, count(td.getDeletedRelationships(db.getNodeById(2), OUTGOING, R2)));
                        assertEquals(1, count(td.getDeletedRelationships(db.getNodeById(2), INCOMING, R2)));
                        assertEquals(0, count(td.getDeletedRelationships(db.getNodeById(2), R3)));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Relationship> deleted = toMap(td.getAllDeletedRelationships());
                        long r4Id = changesToMap(td.getAllChangedNodes()).get(3L).getPrevious().getSingleRelationship(R3, INCOMING).getId();
                        Relationship r4 = deleted.get(r4Id);

                        Relationship deletedRel = td.getDeleted(r4);

                        Node one = deletedRel.getStartNode();
                        assertEquals("One", one.getProperty(NAME));
                        assertEquals(1, one.getProperty(COUNT, 2));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) one.getProperty(TAGS)));
                        assertEquals(3, count(one.getPropertyKeys()));
                        assertEquals(1, count(one.getLabels()));
                        assertTrue(contains(one.getLabels(), label("One")));

                        Node three = deletedRel.getEndNode();
                        assertEquals("Three", three.getProperty(NAME));
                        assertEquals("London", three.getProperty(PLACE));
                        assertEquals("nothing", three.getProperty(TAGS, "nothing"));

                        Relationship r5 = one.getSingleRelationship(R1, OUTGOING);
                        assertEquals("Two", r5.getEndNode().getProperty(NAME));

                        assertEquals(4, count(one.getRelationships()));
                        assertEquals(3, count(one.getRelationships(OUTGOING)));
                        assertEquals(2, count(one.getRelationships(R3)));
                        assertEquals(3, count(one.getRelationships(R3, R1)));
                        assertEquals(1, count(one.getRelationships(INCOMING, R3, R1)));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Relationship> deleted = toMap(td.getAllDeletedRelationships());
                        long r4Id = changesToMap(td.getAllChangedNodes()).get(3L).getPrevious().getSingleRelationship(R3, INCOMING).getId();
                        Relationship deletedRel = td.getDeleted(deleted.get(r4Id));

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Relationship> change = td.getAllChangedRelationships().iterator().next();

                        assertTrue(td.hasPropertyBeenCreated(change.getCurrent(), TAGS));
                        assertFalse(td.hasPropertyBeenCreated(change.getCurrent(), TIME));
                        assertTrue(td.hasPropertyBeenCreated(change.getPrevious(), TAGS));
                        assertFalse(td.hasPropertyBeenCreated(change.getPrevious(), TAG));

                        assertEquals(1, td.createdProperties(change.getCurrent()).size());
                        assertEquals(1, td.createdProperties(change.getPrevious()).size());
                        assertEquals("cool", td.createdProperties(change.getCurrent()).get(TAGS));
                        assertEquals("cool", td.createdProperties(change.getPrevious()).get(TAGS));

                        assertFalse(td.hasPropertyBeenCreated(td.getAllDeletedRelationships().iterator().next(), TAGS));

                        //created relationship should not fall into this category
                        assertFalse(td.hasPropertyBeenCreated(db.getNodeById(7).getSingleRelationship(R2, OUTGOING), TIME));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Relationship> change = td.getAllChangedRelationships().iterator().next();

                        assertTrue(td.hasPropertyBeenChanged(change.getCurrent(), TIME));
                        assertFalse(td.hasPropertyBeenChanged(change.getCurrent(), TAGS));
                        assertTrue(td.hasPropertyBeenChanged(change.getPrevious(), TIME));
                        assertFalse(td.hasPropertyBeenChanged(change.getPrevious(), TAG));

                        assertEquals(1, td.changedProperties(change.getCurrent()).size());
                        assertEquals(1, td.changedProperties(change.getPrevious()).size());
                        assertEquals(3, td.changedProperties(change.getCurrent()).get(TIME).getPrevious());
                        assertEquals(3, td.changedProperties(change.getPrevious()).get(TIME).getPrevious());
                        assertEquals(4, td.changedProperties(change.getCurrent()).get(TIME).getCurrent());
                        assertEquals(4, td.changedProperties(change.getPrevious()).get(TIME).getCurrent());

                        assertFalse(td.hasPropertyBeenChanged(td.getAllDeletedRelationships().iterator().next(), TAGS));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Relationship> change = td.getAllChangedRelationships().iterator().next();

                        assertTrue(td.hasPropertyBeenDeleted(change.getCurrent(), TAG));
                        assertFalse(td.hasPropertyBeenDeleted(change.getCurrent(), TIME));
                        assertTrue(td.hasPropertyBeenDeleted(change.getPrevious(), TAG));
                        assertFalse(td.hasPropertyBeenDeleted(change.getPrevious(), TAGS));

                        assertEquals(1, td.deletedProperties(change.getCurrent()).size());
                        assertEquals(1, td.deletedProperties(change.getPrevious()).size());
                        assertEquals("cool", td.deletedProperties(change.getCurrent()).get(TAG));
                        assertEquals("cool", td.deletedProperties(change.getPrevious()).get(TAG));

                        assertFalse(td.hasPropertyBeenDeleted(td.getAllCreatedRelationships().iterator().next(), TAGS));

                        //deleted relationships' props don't qualify
                        Iterator<Relationship> iterator = td.getAllDeletedRelationships().iterator();
                        assertFalse(td.hasPropertyBeenDeleted(iterator.next(), TIME));
                        assertFalse(td.hasPropertyBeenDeleted(iterator.next(), TIME));
                        assertFalse(td.hasPropertyBeenDeleted(iterator.next(), TIME));
                        assertFalse(td.hasPropertyBeenDeleted(iterator.next(), TIME));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> createdNodes = toMap(td.getAllCreatedNodes());
                        assertEquals(1, createdNodes.size());

                        Node createdNode = createdNodes.get(7L);
                        assertEquals("Seven", createdNode.getProperty(NAME));
                        assertEquals(1, count(createdNode.getLabels()));
                        assertTrue(contains(createdNode.getLabels(), label("SomeLabel")));
                        assertEquals(4L, createdNode.getProperty("size"));
                        assertEquals(2, count(createdNode.getPropertyKeys()));

                        assertTrue(td.hasBeenCreated(createdNode));
                        assertFalse(td.hasBeenCreated(db.getNodeById(3)));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> createdNodes = toMap(td.getAllCreatedNodes());
                        Node createdNode = createdNodes.get(7L);

                        Node three = createdNode.getSingleRelationship(R2, OUTGOING).getEndNode();
                        assertEquals("one", three.getProperty(TAGS));
                        assertFalse(three.getRelationships(R3, INCOMING).iterator().hasNext());

                        Node one = three.getSingleRelationship(R1, INCOMING).getStartNode();
                        assertEquals(1, count(one.getLabels()));
                        assertTrue(contains(one.getLabels(), label("NewOne")));
                    }
                }
        );
    }

    @Test
    public void changedNodesShouldBeCorrectlyIdentified() {
        createTestDatabase();
        try (Transaction tx = db.beginTx()) {
            Node node = db.createNode(label("TestLabel"));
            node.delete();
            tx.success();
        }
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());
                        assertEquals(5, changed.size()); //todo wait for https://github.com/neo4j/neo4j/issues/2534 to be resolved, then change to 4
//                        assertEquals(4, changed.size());

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

                        assertTrue(td.hasBeenChanged(previous1));
                        assertTrue(td.hasBeenChanged(previous2));
                        assertTrue(td.hasBeenChanged(previous3));
                        assertTrue(td.hasBeenChanged(previous4));
                        assertTrue(td.hasBeenChanged(current1));
                        assertTrue(td.hasBeenChanged(current2));
                        assertTrue(td.hasBeenChanged(current3));
                        assertTrue(td.hasBeenChanged(current4));
                        assertFalse(td.hasBeenChanged(db.getNodeById(4)));
                        //assertFalse(td.hasBeenChanged(db.getNodeById(0)));  //todo wait for https://github.com/neo4j/neo4j/issues/2534 to be resolved, then uncomment

                        try {
                            td.getChanged(db.getNodeById(4));
                            fail();
                        } catch (IllegalArgumentException e) {
                            //ok
                        }
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());

                        Node current = changed.get(1L).getCurrent();
                        Node previous = td.getChanged(current).getPrevious();
                        assertEquals(1, count(previous.getLabels()));
                        assertTrue(contains(previous.getLabels(), label("One")));

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());
                        Node current = changed.get(1L).getCurrent();
                        assertEquals(1, count(current.getLabels()));
                        assertTrue(contains(current.getLabels(), label("NewOne")));

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> deletedNodes = toMap(td.getAllDeletedNodes());
                        assertEquals(1, deletedNodes.size());

                        Node deleted = deletedNodes.get(2L);
                        Node one = td.getDeleted(deleted).getSingleRelationship(R1, INCOMING).getStartNode();

                        assertEquals("Two", deleted.getProperty(NAME));
                        assertEquals(2L, deleted.getProperty("size"));
                        assertEquals(2, count(deleted.getPropertyKeys()));

                        assertTrue(td.hasBeenDeleted(deleted));
                        assertFalse(td.hasBeenDeleted(one));

                        try {
                            td.getDeleted(db.getNodeById(4L));
                            fail();
                        } catch (IllegalArgumentException e) {
                            //ok
                        }
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> deletedNodes = toMap(td.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);

                        Node one = td.getDeleted(deleted).getSingleRelationship(R1, INCOMING).getStartNode();
                        assertEquals(1, count(one.getLabels()));
                        assertTrue(contains(one.getLabels(), label("One")));

                        assertEquals(1, one.getSingleRelationship(R1, OUTGOING).getProperty(TIME));
                        assertEquals("Two", one.getRelationships(R1, OUTGOING).iterator().next().getEndNode().getProperty(NAME));
                        assertEquals("Three", one.getRelationships(OUTGOING, R3).iterator().next().getEndNode().getProperty(NAME));
                        assertEquals(2L, one.getRelationships(R1).iterator().next().getEndNode().getProperty("size"));
                    }
                }
        );
    }                                       //todo multiple labels not tested

    @Test
    public void startingWithDeletedNodePreviousGraphVersionShouldBeTraversedUsingTraversalApi() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> deletedNodes = toMap(td.getAllDeletedNodes());
                        Node deleted = deletedNodes.get(2L);

                        Node one = td.getDeleted(deleted).getSingleRelationship(R1, INCOMING).getStartNode();

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
    public void createdNodePropertiesAndLabelsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Node> changed = changesToMap(td.getAllChangedNodes()).get(3L);

                        assertTrue(td.hasPropertyBeenCreated(changed.getCurrent(), TAGS));
                        assertFalse(td.hasPropertyBeenCreated(changed.getCurrent(), NAME));
                        assertTrue(td.hasPropertyBeenCreated(changed.getPrevious(), TAGS));
                        assertFalse(td.hasPropertyBeenCreated(changed.getPrevious(), NAME));

                        assertEquals(1, td.createdProperties(changed.getCurrent()).size());
                        assertEquals(1, td.createdProperties(changed.getPrevious()).size());
                        assertEquals("one", td.createdProperties(changed.getCurrent()).get(TAGS));
                        assertEquals("one", td.createdProperties(changed.getPrevious()).get(TAGS));

                        assertFalse(td.hasPropertyBeenCreated(changesToMap(td.getAllChangedNodes()).get(1L).getCurrent(), TAGS));

                        Change<Node> changed1 = changesToMap(td.getAllChangedNodes()).get(1L);
                        assertTrue(td.hasLabelBeenAssigned(changed1.getCurrent(), label("NewOne")));
                        assertTrue(td.hasLabelBeenAssigned(changed1.getPrevious(), label("NewOne")));
                        assertFalse(td.hasLabelBeenAssigned(changed1.getCurrent(), label("One")));
                        assertFalse(td.hasLabelBeenAssigned(changed1.getPrevious(), label("One")));
                        assertEquals(1, td.assignedLabels(changed1.getCurrent()).size());
                        assertEquals(1, td.assignedLabels(changed1.getPrevious()).size());
                        assertEquals("NewOne", getSingle(td.assignedLabels(changed1.getCurrent())).name());
                        assertEquals("NewOne", getSingle(td.assignedLabels(changed1.getPrevious())).name());

                        Change<Node> changed5 = changesToMap(td.getAllChangedNodes()).get(5L);
                        assertTrue(td.hasLabelBeenAssigned(changed5.getCurrent(), label("NewLabel")));
                        assertTrue(td.hasLabelBeenAssigned(changed5.getPrevious(), label("NewLabel")));
                        assertFalse(td.hasLabelBeenAssigned(changed5.getCurrent(), label("SomeLabel")));
                        assertFalse(td.hasLabelBeenAssigned(changed5.getPrevious(), label("SomeLabel")));
                        assertEquals(1, td.assignedLabels(changed5.getCurrent()).size());
                        assertEquals(1, td.assignedLabels(changed5.getPrevious()).size());
                        assertEquals("NewLabel", getSingle(td.assignedLabels(changed5.getCurrent())).name());
                        assertEquals("NewLabel", getSingle(td.assignedLabels(changed5.getPrevious())).name());

                        //not changed at all
                        assertTrue(td.createdProperties(db.getNodeById(4)).isEmpty());
                        assertTrue(td.deletedProperties(db.getNodeById(4)).isEmpty());
                        assertTrue(td.changedProperties(db.getNodeById(4)).isEmpty());
                    }
                }
        );
    }

    @Test
    public void changedNodePropertiesAndLabelsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Node> changed = changesToMap(td.getAllChangedNodes()).get(1L);

                        assertTrue(td.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertTrue(td.hasPropertyBeenChanged(changed.getCurrent(), TAGS));
                        assertFalse(td.hasPropertyBeenChanged(changed.getCurrent(), COUNT));
                        assertTrue(td.hasPropertyBeenChanged(changed.getPrevious(), NAME));
                        assertTrue(td.hasPropertyBeenChanged(changed.getPrevious(), TAGS));
                        assertFalse(td.hasPropertyBeenChanged(changed.getPrevious(), COUNT));

                        assertEquals(2, td.changedProperties(changed.getCurrent()).size());
                        assertEquals(2, td.changedProperties(changed.getPrevious()).size());
                        assertEquals("One", td.changedProperties(changed.getCurrent()).get(NAME).getPrevious());
                        assertEquals("One", td.changedProperties(changed.getPrevious()).get(NAME).getPrevious());
                        assertEquals("NewOne", td.changedProperties(changed.getCurrent()).get(NAME).getCurrent());
                        assertEquals("NewOne", td.changedProperties(changed.getPrevious()).get(NAME).getCurrent());
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) td.changedProperties(changed.getCurrent()).get(TAGS).getCurrent()));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) td.changedProperties(changed.getPrevious()).get(TAGS).getPrevious()));
                        assertTrue(Arrays.equals(new String[]{"one", "three"}, (String[]) td.changedProperties(changed.getCurrent()).get(TAGS).getCurrent()));
                        assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) td.changedProperties(changed.getPrevious()).get(TAGS).getPrevious()));

                        assertEquals(3, count(changed.getPrevious().getPropertyKeys()));
                        assertEquals(2, count(changed.getCurrent().getPropertyKeys()));

                        changed = changesToMap(td.getAllChangedNodes()).get(3L);
                        assertEquals(0, td.changedProperties(changed.getCurrent()).size());
                        assertEquals(0, td.changedProperties(changed.getPrevious()).size());
                        assertFalse(td.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertFalse(td.hasPropertyBeenChanged(changed.getCurrent(), TAGS));
                        assertFalse(td.hasPropertyBeenChanged(changed.getCurrent(), PLACE));
                        assertFalse(td.hasPropertyBeenChanged(changed.getPrevious(), NAME));
                        assertFalse(td.hasPropertyBeenChanged(changed.getPrevious(), TAGS));
                        assertFalse(td.hasPropertyBeenChanged(changed.getPrevious(), PLACE));

                        assertFalse(td.hasPropertyBeenChanged(getSingle(td.getAllDeletedNodes()), NAME));
                        assertFalse(td.hasPropertyBeenChanged(getSingle(td.getAllCreatedNodes()), NAME));

                        assertEquals(2, count(changed.getPrevious().getPropertyKeys()));
                        assertEquals(3, count(changed.getCurrent().getPropertyKeys()));

                        //one that isn't changed
                        Node unchanged = changesToMap(td.getAllChangedNodes()).get(1L).getPrevious().getSingleRelationship(R3, OUTGOING).getEndNode().getSingleRelationship(R1, OUTGOING).getEndNode();
                        assertEquals(1, count(unchanged.getPropertyKeys()));
                        assertEquals(NAME, unchanged.getPropertyKeys().iterator().next());
                        assertEquals("Four", unchanged.getProperty(NAME));
                        assertEquals("Four", unchanged.getProperty(NAME, "nothing"));
                        assertEquals("nothing", unchanged.getProperty("non-existing", "nothing"));

                        //labels changed
                        changed = changesToMap(td.getAllChangedNodes()).get(5L);
                        assertEquals(0, td.changedProperties(changed.getCurrent()).size());
                        assertEquals(0, td.changedProperties(changed.getPrevious()).size());
                        assertFalse(td.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertEquals(1, td.assignedLabels(changed.getPrevious()).size());
                        assertEquals(1, td.assignedLabels(changed.getCurrent()).size());
                        assertEquals("NewLabel", getSingle(td.assignedLabels(changed.getPrevious())).name());
                        assertEquals("NewLabel", getSingle(td.assignedLabels(changed.getCurrent())).name());
                        assertTrue(td.hasLabelBeenAssigned(changed.getPrevious(), label("NewLabel")));
                        assertTrue(td.hasLabelBeenAssigned(changed.getCurrent(), label("NewLabel")));
                        assertFalse(td.hasLabelBeenAssigned(changed.getPrevious(), label("SomeOther")));
                        assertFalse(td.hasLabelBeenAssigned(changed.getCurrent(), label("SomeOther")));
                        assertFalse(td.hasLabelBeenRemoved(changed.getPrevious(), label("NewLabel")));
                        assertFalse(td.hasLabelBeenRemoved(changed.getCurrent(), label("NewLabel")));
                        assertEquals(0, td.removedLabels(changed.getPrevious()).size());
                        assertEquals(0, td.removedLabels(changed.getCurrent()).size());

                        changed = changesToMap(td.getAllChangedNodes()).get(6L);
                        assertEquals(0, td.changedProperties(changed.getCurrent()).size());
                        assertEquals(0, td.changedProperties(changed.getPrevious()).size());
                        assertFalse(td.hasPropertyBeenChanged(changed.getCurrent(), NAME));
                        assertEquals(1, td.removedLabels(changed.getPrevious()).size());
                        assertEquals(1, td.removedLabels(changed.getCurrent()).size());
                        assertEquals("ToBeRemoved", getSingle(td.removedLabels(changed.getPrevious())).name());
                        assertEquals("ToBeRemoved", getSingle(td.removedLabels(changed.getCurrent())).name());
                        assertTrue(td.hasLabelBeenRemoved(changed.getPrevious(), label("ToBeRemoved")));
                        assertTrue(td.hasLabelBeenRemoved(changed.getCurrent(), label("ToBeRemoved")));
                        assertFalse(td.hasLabelBeenRemoved(changed.getPrevious(), label("SomeOther")));
                        assertFalse(td.hasLabelBeenRemoved(changed.getCurrent(), label("SomeOther")));
                        assertFalse(td.hasLabelBeenAssigned(changed.getPrevious(), label("NewLabel")));
                        assertFalse(td.hasLabelBeenAssigned(changed.getCurrent(), label("NewLabel")));
                        assertEquals(0, td.assignedLabels(changed.getPrevious()).size());
                        assertEquals(0, td.assignedLabels(changed.getCurrent()).size());

                        assertTrue(td.assignedLabels(db.getNodeById(4)).isEmpty());
                        assertTrue(td.removedLabels(db.getNodeById(4)).isEmpty());
                        assertFalse(td.hasLabelBeenAssigned(db.getNodeById(4), label("any")));
                        assertFalse(td.hasLabelBeenRemoved(db.getNodeById(4), label("any")));
                    }
                }
        );
    }

    @Test
    public void deletedNodePropertiesAndLabelsShouldBeCorrectlyIdentified() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Node> changed = changesToMap(td.getAllChangedNodes()).get(1L);

                        assertFalse(td.hasPropertyBeenDeleted(changed.getCurrent(), NAME));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getCurrent(), TAGS));
                        assertTrue(td.hasPropertyBeenDeleted(changed.getCurrent(), COUNT));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getPrevious(), NAME));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getPrevious(), TAGS));
                        assertTrue(td.hasPropertyBeenDeleted(changed.getPrevious(), COUNT));

                        assertEquals(1, td.deletedProperties(changed.getCurrent()).size());
                        assertEquals(1, td.deletedProperties(changed.getPrevious()).size());
                        assertEquals(1, td.deletedProperties(changed.getCurrent()).get(COUNT));
                        assertEquals(1, td.deletedProperties(changed.getPrevious()).get(COUNT));

                        assertTrue(td.hasLabelBeenRemoved(changed.getCurrent(), label("One")));
                        assertTrue(td.hasLabelBeenRemoved(changed.getPrevious(), label("One")));
                        assertEquals(1, td.removedLabels(changed.getCurrent()).size());
                        assertEquals(1, td.removedLabels(changed.getPrevious()).size());
                        assertEquals("One", getSingle(td.removedLabels(changed.getCurrent())).name());
                        assertEquals("One", getSingle(td.removedLabels(changed.getPrevious())).name());

                        changed = changesToMap(td.getAllChangedNodes()).get(3L);
                        assertEquals(0, td.deletedProperties(changed.getCurrent()).size());
                        assertEquals(0, td.deletedProperties(changed.getPrevious()).size());
                        assertFalse(td.hasPropertyBeenDeleted(changed.getCurrent(), NAME));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getCurrent(), TAGS));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getCurrent(), PLACE));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getPrevious(), NAME));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getPrevious(), TAGS));
                        assertFalse(td.hasPropertyBeenDeleted(changed.getPrevious(), PLACE));

                        assertFalse(td.hasPropertyBeenDeleted(getSingle(td.getAllDeletedNodes()), NAME));
                        assertFalse(td.hasPropertyBeenDeleted(getSingle(td.getAllCreatedNodes()), NAME));

                        changed = changesToMap(td.getAllChangedNodes()).get(6L);
                        assertTrue(td.hasLabelBeenRemoved(changed.getCurrent(), label("ToBeRemoved")));
                        assertTrue(td.hasLabelBeenRemoved(changed.getPrevious(), label("ToBeRemoved")));
                        assertEquals(1, td.removedLabels(changed.getCurrent()).size());
                        assertEquals(1, td.removedLabels(changed.getPrevious()).size());
                        assertEquals("ToBeRemoved", getSingle(td.removedLabels(changed.getCurrent())).name());
                        assertEquals("ToBeRemoved", getSingle(td.removedLabels(changed.getPrevious())).name());

                        assertTrue(td.assignedLabels(db.getNodeById(4)).isEmpty());
                        assertTrue(td.removedLabels(db.getNodeById(4)).isEmpty());
                        assertFalse(td.hasLabelBeenAssigned(db.getNodeById(4), label("any")));
                        assertFalse(td.hasLabelBeenRemoved(db.getNodeById(4), label("any")));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Relationship> created = toMap(td.getAllCreatedRelationships());

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Node> createdNodes = toMap(td.getAllCreatedNodes());
                        Node createdNode = createdNodes.get(7L);

                        createdNode.setProperty(NAME, "NewSeven");
                        createdNode.setProperty("additional", "something");
                        createdNode.removeProperty("size");
                        createdNode.addLabel(label("SomeNewLabel"));
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {

            Node createdNode = db.getNodeById(7L);

            assertEquals("NewSeven", createdNode.getProperty(NAME));
            assertEquals("something", createdNode.getProperty("additional"));
            assertEquals(2, count(createdNode.getPropertyKeys()));
            assertFalse(createdNode.hasProperty("size"));
            assertTrue(createdNode.hasLabel(label("SomeNewLabel")));
        }
    }

    @Test
    public void shouldBeAbleToChangeCurrentChangedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Relationship>> changed = changesToMap(td.getAllChangedRelationships());

                        Relationship r = getSingle(changed.entrySet()).getValue().getCurrent();

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Relationship>> changed = changesToMap(td.getAllChangedRelationships());

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        node.setProperty(NAME, "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty(TAGS);
                        node.removeLabel(label("NewOne"));
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {

            Node node = db.getNodeById(1L);

            assertEquals(2, count(node.getPropertyKeys()));
            assertEquals("YetAnotherOne", node.getProperty(NAME));
            assertEquals("something", node.getProperty("additional"));
            assertFalse(node.hasProperty(TAGS));
            assertEquals(0, count(node.getLabels()));
        }
    }

    @Test
    public void shouldBeAbleToChangePreviousChangedNodeBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        node.setProperty(NAME, "YetAnotherOne");
                        node.setProperty("additional", "something");
                        node.removeProperty(TAGS);
                        node.addLabel(label("YetAnotherLabel"));
                        node.removeLabel(label("NewOne"));
                    }
                }
        );

        try (Transaction tx = db.beginTx()) {

            Node node = db.getNodeById(1L);

            assertEquals(2, count(node.getPropertyKeys()));
            assertEquals("YetAnotherOne", node.getProperty(NAME));
            assertEquals("something", node.getProperty("additional"));
            assertFalse(node.hasProperty(TAGS));
            assertEquals(1, count(node.getLabels()));
            assertTrue(node.hasLabel(label("YetAnotherLabel")));
        }
    }

    @Test(expected = TransactionFailureException.class)
    public void shouldNotBeAbleToChangeDeletedRelationshipBeforeCommit() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Relationship> deleted = toMap(td.getAllDeletedRelationships());

                        long r1Id = toMap(td.getAllDeletedNodes()).get(2L).getSingleRelationship(R1, INCOMING).getId();
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> deletedNodes = toMap(td.getAllDeletedNodes());

                        Node deleted = deletedNodes.get(2L);

                        try {
                            deleted.setProperty("irrelevant", "irrelevant");
                            return;
                        } catch (IllegalStateException e) {
                            //OK
                        }

                        try {
                            deleted.addLabel(label("irrelevant"));
                            return;
                        } catch (IllegalStateException e) {
                            //OK
                        }

                        try {
                            deleted.removeLabel(label("irrelevant"));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> deletedNodes = toMap(td.getAllDeletedNodes());

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> deletedNodes = toMap(td.getAllDeletedNodes());

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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        throw new RuntimeException("Deliberate testing exception");
                    }
                },
                KeepCalmAndCarryOn.getInstance()
        );

        try (Transaction tx = db.beginTx()) {

            long r4Id = db.getNodeById(3L).getSingleRelationship(R3, INCOMING).getId();
            Relationship r4 = db.getRelationshipById(r4Id);

            Node one = r4.getStartNode();
            assertEquals("One", one.getProperty(NAME));
            assertEquals(1, one.getProperty(COUNT, 2));
            assertTrue(Arrays.equals(new String[]{"one", "two"}, (String[]) one.getProperty(TAGS)));
            assertEquals(3, count(one.getPropertyKeys()));
            assertEquals(1, count(one.getLabels()));
            assertTrue(contains(one.getLabels(), label("One")));

            assertEquals("Three", r4.getEndNode().getProperty(NAME));
            assertEquals("London", r4.getEndNode().getProperty(PLACE));
            assertEquals("nothing", r4.getEndNode().getProperty(TAGS, "nothing"));

            Relationship r5 = one.getSingleRelationship(R1, OUTGOING);
            assertEquals("Two", r5.getEndNode().getProperty(NAME));

            assertEquals(4, count(one.getRelationships()));
            assertEquals(3, count(one.getRelationships(OUTGOING)));
            assertEquals(2, count(one.getRelationships(R3)));
            assertEquals(3, count(one.getRelationships(R3, R1)));
            assertEquals(1, count(one.getRelationships(INCOMING, R3, R1)));
        }
    }

    @Test
    public void shouldBeAbleToDeleteChangedNodeCommittingTransaction() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Node> change = td.getChanged(db.getNodeById(1));
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Map<Long, Node> deletedNodes = toMap(td.getAllDeletedNodes());
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
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());

                        Node node = changed.get(1L).getCurrent();

                        Node newNode = db.createNode(label("NewNode"));
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
            assertTrue(contains(newRelationship.getEndNode().getLabels(), label("NewNode")));
        }
    }

    @Test
    public void shouldBeAbleToCreateAdditionalNodesAndRelationshipsFromPreviousGraphVersion() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        if (!td.mutationsOccurred()) {
                            return;
                        }

                        Map<Long, Change<Node>> changed = changesToMap(td.getAllChangedNodes());

                        Node node = changed.get(1L).getPrevious();

                        Node newNode = db.createNode(label("NewNode"));
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
            assertTrue(contains(newRelationship.getEndNode().getLabels(), label("NewNode")));
        }
    }

    @Test
    public void propertyExtractionStrategySmokeTest() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Relationship previous = getSingle(td.getAllChangedRelationships()).getPrevious();
                        Relationship current = getSingle(td.getAllChangedRelationships()).getCurrent();

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
                        public void beforeCommit(ImprovedTransactionData td) {
                            assertFalse(td.mutationsOccurred());
                        }

                        @Override
                        public boolean mutationsOccurred() {
                            return true;
                        }
                    }
        );
    }

    @Test
    public void verifyDegrees() {
        createTestDatabase();
        mutateGraph(
                new BeforeCommitCallback.RememberingAdapter() {
                    @Override
                    public void doBeforeCommit(ImprovedTransactionData td) {
                        Change<Node> changed = changesToMap(td.getAllChangedNodes()).get(1L);

                        Node oneCurrent = changed.getCurrent();
                        assertEquals(3, oneCurrent.getDegree());
                        assertEquals(2, oneCurrent.getDegree(OUTGOING));
                        assertEquals(1, oneCurrent.getDegree(INCOMING));
                        assertEquals(1, oneCurrent.getDegree(R3));
                        assertEquals(1, oneCurrent.getDegree(R3, INCOMING));
                        assertEquals(1, oneCurrent.getDegree(R1));
                        assertEquals(0, oneCurrent.getDegree(R3, OUTGOING));
                        assertEquals(0, oneCurrent.getDegree(R4));

                        Node onePrevious = changed.getPrevious();
                        assertEquals(4, onePrevious.getDegree());
                        assertEquals(3, onePrevious.getDegree(OUTGOING));
                        assertEquals(1, onePrevious.getDegree(INCOMING));
                        assertEquals(2, onePrevious.getDegree(R3));
                        assertEquals(1, onePrevious.getDegree(R3, INCOMING));
                        assertEquals(1, onePrevious.getDegree(R3, OUTGOING));
                        assertEquals(1, onePrevious.getDegree(R1, OUTGOING));
                        assertEquals(0, onePrevious.getDegree(R1, INCOMING));

                        Node two = td.getDeleted(db.getNodeById(2L));
                        assertEquals(3, two.getDegree()); //todo loops only count as 1 - emailed Neo if this is a deliberate choice
                        assertEquals(2, two.getDegree(R2));
                        assertEquals(1, two.getDegree(R2, INCOMING));
                        assertEquals(2, two.getDegree(R2, OUTGOING));
                        assertEquals(2, two.getDegree(INCOMING));
                        assertEquals(2, two.getDegree(OUTGOING));
                    }
                }
        );
    }

    //test helpers

    protected void mutateGraph(BeforeCommitCallback.RememberingAdapter beforeCommitCallback) {
        mutateGraph(new TestGraphMutation(), beforeCommitCallback);
        assertTrue(beforeCommitCallback.mutationsOccurred);
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
                .node(label("One")).setProp(NAME, "One").setProp(COUNT, 1).setProp(TAGS, new String[]{"one", "two"})

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
                .relationshipFrom(4, R4)
                .node(label("ToBeRemoved")).setProp(NAME, "Six")
                .relationshipFrom(5, R4);
    }

    private class TestGraphMutation extends VoidReturningCallback {

        @Override
        public void doInTx(GraphDatabaseService database) {
            Node one = database.getNodeById(1);
            one.setProperty(NAME, "NewOne");
            one.removeProperty(COUNT);
            one.setProperty(TAGS, new String[]{"one"});
            one.setProperty(TAGS, new String[]{"one", "three"});
            one.removeLabel(label("One"));
            one.addLabel(label("NewOne"));

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

            Node six = database.getNodeById(6);
            six.removeLabel(label("ToBeRemoved"));

            //this should not be picked up as a change
            Node zero = database.getNodeById(0);
            zero.removeLabel(label("TestLabel"));
            zero.addLabel(label("Temp"));
            zero.removeLabel(label("Temp"));
            zero.addLabel(label("TestLabel"));
        }
    }

    /**
     * just for this test so the lines aren't that long
     *
     * @param propertyContainers
     * @param <T>
     * @return
     */
    private static <T extends PropertyContainer> Map<Long, T> toMap(Collection<T> propertyContainers) {
        return propertyContainersToMap(propertyContainers);
    }

    private static <K, V> V getSingleValue(Map<K, V> map) {
        return getSingle(map.entrySet()).getValue();
    }
}
