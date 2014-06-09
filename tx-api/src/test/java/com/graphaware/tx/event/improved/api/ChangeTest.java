package com.graphaware.tx.event.improved.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.tx.event.improved.api.Change.changesToMap;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 *  Unit test for {@link Change}.
 */
public class ChangeTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        new ExecutionEngine(database).execute("CREATE " +
                "(a), " +
                "(b {key:'value'})," +
                "(b)-[:test]->(a)," +
                "(c {key:'value'})");
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldConvertChangesToMap() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange = new Change<>(database.getNodeById(0), database.getNodeById(0));
            Map<Long, Change<Node>> changeMap = changesToMap(asList(nodeChange));
            assertEquals(0, changeMap.get(0L).getCurrent().getId());
            assertEquals(0, changeMap.get(0L).getPrevious().getId());
            assertEquals(1, changeMap.size());
        }
    }

    @Test
    public void equalChangesShouldBeEqual() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange1 = new Change<>(database.getNodeById(0), database.getNodeById(0));
            Change<Node> nodeChange2 = new Change<>(database.getNodeById(0), database.getNodeById(0));
            Change<Node> nodeChange3 = new Change<>(database.getNodeById(1), database.getNodeById(1));

            assertTrue(nodeChange1.equals(nodeChange2));
            assertTrue(nodeChange2.equals(nodeChange1));
            assertFalse(nodeChange3.equals(nodeChange1));
            assertFalse(nodeChange1.equals(nodeChange3));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidChangeShouldThrowException() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange1 = new Change<>(database.getNodeById(0), database.getNodeById(1));
            changesToMap(Collections.singleton(nodeChange1));
        }
    }
}
