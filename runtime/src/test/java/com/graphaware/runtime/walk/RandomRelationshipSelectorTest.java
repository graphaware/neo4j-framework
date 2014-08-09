package com.graphaware.runtime.walk;

import com.graphaware.common.strategy.IncludeAllNodes;
import com.graphaware.common.strategy.IncludeNodesWithLabel;
import com.graphaware.common.strategy.IncludeRelationships;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link RandomRelationshipSelector}.
 */
public class RandomRelationshipSelectorTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldReturnNullOnNodeWithNoRelationships() {
        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomRelationshipSelector().selectRelationship(database.getNodeById(0)));
            tx.success();
        }
    }

    @Test
    public void shouldReturnNullOnNodeWithNoMatchingRelationships() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            Node node2 = database.createNode();
            node1.createRelationshipTo(node2, withName("TEST"));
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertNull(new RandomRelationshipSelector(new IncludeRelationships(withName("NOT_EXIST")), IncludeAllNodes.getInstance()).selectRelationship(database.getNodeById(0)));
            assertNull(new RandomRelationshipSelector(IncludeRelationships.INCOMING, IncludeAllNodes.getInstance()).selectRelationship(database.getNodeById(0)));
            assertNull(new RandomRelationshipSelector(IncludeRelationships.OUTGOING, new IncludeNodesWithLabel(DynamicLabel.label("Test"))).selectRelationship(database.getNodeById(0)));
            tx.success();
        }
    }

    @Test
    public void shouldReturnSingleMatchingRelationship() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            Node node2 = database.createNode(DynamicLabel.label("Test"));
            node1.createRelationshipTo(node2, withName("TEST"));
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new RandomRelationshipSelector(IncludeRelationships.OUTGOING, IncludeAllNodes.getInstance()).selectRelationship(database.getNodeById(0)).getId());
            assertEquals(0,new RandomRelationshipSelector(IncludeRelationships.OUTGOING, new IncludeNodesWithLabel(DynamicLabel.label("Test"))).selectRelationship(database.getNodeById(0)).getId());
            tx.success();
        }
    }
}
