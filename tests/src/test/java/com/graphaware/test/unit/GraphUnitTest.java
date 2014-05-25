/*
 * Copyright (c) 2014 GraphAware
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

package com.graphaware.test.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static com.graphaware.test.unit.GraphUnit.assertSubgraph;

/**
 * Unit test for {@link com.graphaware.test.unit.GraphUnit}.
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

    @Test(expected = AssertionError.class)
    public void mappedRelationshipsShouldNotBeReused() {
        populateDatabase("CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:2013})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:FIRST]->(month:Month {value:5})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:4})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day)");

        assertSameGraph(database, "CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:2013})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:CHILD]->(month:Month {value:5})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:4})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day)");
    }

    @Test(expected = AssertionError.class)
    public void mappedRelationshipsShouldNotBeReused2() {
        populateDatabase("CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:2013})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:FIRST]->(month:Month {value:5})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:4})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day)");

        assertSubgraph(database, "CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:2013})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:CHILD]->(month:Month {value:5})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:4})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day)");
    }

    @Test
    public void deletedRelationshipWithNewTypeShouldNotInfluenceEquality() { //bug test
        try (Transaction tx = database.beginTx()) {
            Node node1 = database.createNode();
            Node node2 = database.createNode();
            node1.createRelationshipTo(node2, DynamicRelationshipType.withName("ACCIDENT"));
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            GlobalGraphOperations.at(database).getAllRelationships().iterator().next().delete();
            tx.success();
        }

        String cypher = "CREATE (n), (m)";

        assertSameGraph(database, cypher);
    }

    @Test
    public void deletedNewLabelShouldNotInfluenceEquality() { //bug test
        try (Transaction tx = database.beginTx()) {
            database.createNode(DynamicLabel.label("Accident"));
            database.createNode(DynamicLabel.label("RealLabel"));
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            database.getNodeById(0).delete();
            tx.success();
        }

        String cypher = "CREATE (n:RealLabel)";

        assertSameGraph(database, cypher);
    }

    @Test
    public void deletedNewPropsShouldNotInfluenceEquality() { //bug test
        try (Transaction tx = database.beginTx()) {
            database.createNode().setProperty("accident", "dummy");
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            database.getNodeById(0).delete();
            tx.success();
        }

        String cypher = "CREATE (n)";

        assertSameGraph(database, cypher);
    }
}
