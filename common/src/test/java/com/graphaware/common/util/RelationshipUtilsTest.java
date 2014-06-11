package com.graphaware.common.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import static com.graphaware.common.util.RelationshipUtils.*;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.*;

/**
 * Unit test for {@link RelationshipUtils}.
 */
public class RelationshipUtilsTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            Node node2 = database.createNode();
            node1.createRelationshipTo(node2, withName("TEST"));
            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void nonExistingRelationshipShouldBeCorrectlyIdentified() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.getNodeById(0);
            Node node2 = database.getNodeById(1);

            assertTrue(relationshipNotExists(node2, node1, withName("TEST"), OUTGOING));
            assertTrue(relationshipNotExists(node2, node1, withName("IDONTEXIST"), INCOMING));
            assertFalse(relationshipNotExists(node2, node1, withName("TEST"), INCOMING));
            assertFalse(relationshipNotExists(node2, node1, withName("TEST"), BOTH));

            tx.success();
        }
    }

    @Test(expected = NotFoundException.class)
    public void nonExistingRelationshipShouldBeCorrectlyIdentified2() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.getNodeById(0);
            Node node2 = database.getNodeById(1);

            getSingleRelationship(node2, node1, withName("IDONTEXIST"), INCOMING);

            tx.success();
        }
    }

    @Test
    public void existingRelationshipShouldNotBeRecreated() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.getNodeById(0);
            Node node2 = database.getNodeById(1);

            Relationship r = createRelationshipIfNotExists(node1, node2, withName("TEST"), OUTGOING);
            assertEquals(0, r.getId());
            assertEquals(1, IterableUtils.count(GlobalGraphOperations.at(database).getAllRelationships()));

            tx.success();
        }
    }

    @Test
    public void nonExistingRelationshipShouldBeCreated() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.getNodeById(0);
            Node node2 = database.getNodeById(1);

            Relationship r = createRelationshipIfNotExists(node2, node1, withName("TEST"), OUTGOING);
            assertEquals(1, r.getId());
            assertEquals(2, IterableUtils.count(GlobalGraphOperations.at(database).getAllRelationships()));

            tx.success();
        }
    }

    @Test
    public void existingRelationshipShouldBeDeleted() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.getNodeById(0);
            Node node2 = database.getNodeById(1);

            deleteRelationshipIfExists(node1, node2, withName("TEST"), OUTGOING);
            assertEquals(0, IterableUtils.count(GlobalGraphOperations.at(database).getAllRelationships()));

            tx.success();
        }
    }

    @Test
    public void nonExistingRelationshipDeletionShouldDoNothing() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.getNodeById(0);
            Node node2 = database.getNodeById(1);

            deleteRelationshipIfExists(node2, node1, withName("TEST"), OUTGOING);
            assertEquals(1, IterableUtils.count(GlobalGraphOperations.at(database).getAllRelationships()));

            tx.success();
        }
    }
}
