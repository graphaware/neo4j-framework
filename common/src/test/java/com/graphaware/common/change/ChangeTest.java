package com.graphaware.common.change;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Map;

import static com.graphaware.common.change.Change.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

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
}
