package com.graphaware.graphunit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Date;

import static com.graphaware.graphunit.GraphUnit.*;

/**
 * Unit test for {@link GraphUnit}.
 */
public class GraphUnitTest {

    private GraphDatabaseService database;
    private ExecutionEngine executionEngine;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        executionEngine = new ExecutionEngine(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    private void populateDatabase(String cypher) {
        executionEngine.execute(cypher);
    }

    @Test
    public void equalGraphsWithLabelsShouldPassSameGraphTest() {
        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(assertCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test
    public void equalGraphsWithLabelsShouldPassSubgraphTest() {
        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(assertCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void equalGraphsWithoutLabelsShouldPassSameGraphTest() {
        String assertCypher = "CREATE \n" +
                "(m {name:'Michal'})-[:WORKS_FOR]->(c {name:'GraphAware'}),\n" +
                "(d {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(assertCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test
    public void equalGraphsWithoutLabelsShouldPassSubgraphTest() {
        String assertCypher = "CREATE \n" +
                "(m {name:'Michal'})-[:WORKS_FOR]->(c {name:'GraphAware'}),\n" +
                "(d {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(assertCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void equalSingleNodeGraphsShouldPassSameGraphTest() {
        populateDatabase("CREATE (m:Person {name:'Michal'})");

        assertSameGraph(database, "CREATE (m:Person {name:'Michal'})");
    }

    @Test
    public void equalSingleNodeGraphsShouldPassSubgraphTest() {
        populateDatabase("CREATE (m:Person {name:'Michal'})");

        assertSubgraph(database, "CREATE (m:Person {name:'Michal'})");
    }

    @Test(expected = AssertionError.class)
    public void singleNodeGraphsWithDifferentLabelsShouldFailSameGraphTest3() {
        populateDatabase("CREATE (m:Male {name:'Michal'}), (d:Female {name:'Daniela'})");

        assertSameGraph(database, "CREATE (m:Female {name:'Michal'}), (d:Male {name:'Daniela'})");
    }

    @Test(expected = AssertionError.class)
    public void singleNodeGraphsWithDifferentLabelsShouldFailSubgraphTest2() {
        populateDatabase("CREATE (m:Male {name:'Michal'}), (d:Female {name:'Daniela'})");

        assertSubgraph(database, "CREATE (m:Female {name:'Michal'}), (d:Male {name:'Daniela'})");
    }

    @Test(expected = AssertionError.class)
    public void singleNodeGraphsWithDifferentLabelsShouldFailSameGraphTest() {
        populateDatabase("CREATE (m:Person:Human {name:'Michal'})");

        assertSameGraph(database, "CREATE (m:Person {name:'Michal'})");
    }

    @Test(expected = AssertionError.class)
    public void singleNodeGraphsWithDifferentLabelsShouldFailSubgraphTest() {
        populateDatabase("CREATE (m:Person:Human {name:'Michal'})");

        assertSubgraph(database, "CREATE (m:Person {name:'Michal'})");
    }

    @Test(expected = AssertionError.class)
    public void singleNodeGraphsWithDifferentLabelsShouldFailSameGraphTest2() {
        populateDatabase("CREATE (m:Person {name:'Michal'})");

        assertSameGraph(database, "CREATE (m:Person:Human {name:'Michal'})");
    }

    @Test(expected = AssertionError.class)
    public void extraRelationshipPropertyShouldFailSameGraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR {since:2014}]->(c)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void extraRelationshipPropertyShouldFailSubgraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR {since:2014}]->(c)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void differentRelTypesShouldFailSameGraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR1]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR2]->(c)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR2]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR1]->(c)";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void differentRelTypesShouldFailSubgraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR1]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR2]->(c)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR2]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR1]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void extraRelationshipPropertyShouldFailSameGraphTest2() {
        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR {since:2014}]->(c)";

        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void missingRelationshipShouldFailSameGraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void missingRelationshipShouldFailSameGraphTest2() {
        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void missingRelationshipInDatabaseShouldFailSubgraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void missingRelationshipInSubgraphShouldPassSubgraphTest() {
        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void missingNodePropertyShouldFailSameGraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void missingNodePropertyShouldFailSubgraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void correctSubgraphShouldPassSubgraphTest() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)\n," +
                "(:Person {name:'Adam'})-[:WORKS_FOR]->(c)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void correctSubgraphShouldPassSubgraphTest2() {
        String populateCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c),\n" +
                "(:Person {name:'Adam'})-[:WORKS_FOR]->(c),\n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c2:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c2),\n" +
                "(:Person {name:'Adam'})-[:WORKS_FOR]->(c2)";

        String assertCypher = "CREATE \n" +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'}),\n" +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void equalGraphsShouldPassSubgraphTest() {
        String cypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(cypher);

        assertSubgraph(database, cypher);
    }

    @Test
    public void equalGraphsShouldPassSameGraphTest() {
        String cypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(cypher);

        assertSameGraph(database, cypher);
    }

    @Test(expected = AssertionError.class)
    public void inCorrectSubgraphShouldFailSubgraphTest() {
        String populateCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String assertCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void inCorrectSubgraphShouldFailSubgraphTest2() {
        String assertCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String populateCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void notEqualGraphsShouldFailTheSameGraphTest() {
        String populateCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String assertCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test(expected = AssertionError.class)
    public void notEqualGraphsShouldFailTheSameGraphTest2() {
        String assertCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String populateCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test
    public void equalNumbersWithDifferentTypeShouldBeEqual() {
        try (Transaction tx = database.beginTx()) {
            database.createNode().setProperty("number", 123);
            tx.success();
        }

        String cypher = "CREATE (n {number:123})";

        assertSameGraph(database, cypher);
    }

    @Test
    public void equalArraysWithDifferentTypeShouldBeEqual() {
        try (Transaction tx = database.beginTx()) {
            database.createNode().setProperty("number", new int[]{123,124});
            tx.success();
        }

        String cypher = "CREATE (n {number:[123,124]})";

        assertSameGraph(database, cypher);
    }
}
