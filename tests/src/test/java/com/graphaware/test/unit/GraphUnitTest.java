/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.test.unit;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.common.policy.inclusion.*;
import com.graphaware.common.policy.inclusion.none.IncludeNoNodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;

import static com.graphaware.common.util.IterableUtils.count;
import static com.graphaware.test.unit.GraphUnit.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit test for {@link com.graphaware.test.unit.GraphUnit}.
 */
@ExtendWith(Neo4jExtension.class)
public class GraphUnitTest {

    @InjectNeo4j
    protected GraphDatabaseService database;

    private void populateDatabase(String cypher) {
        database.executeTransactionally(cypher);
    }

    @Test
    public void equalGraphsWithLabelsShouldPassSameGraphTest() {
        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(assertCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test
    public void equalGraphsWithLabelsShouldPassSubgraphTest() {
        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(assertCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void equalGraphsWithoutLabelsShouldPassSameGraphTest() {
        String assertCypher = "CREATE " +
                "(m {name:'Michal'})-[:WORKS_FOR]->(c {name:'GraphAware'})," +
                "(d {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(assertCypher);

        assertSameGraph(database, assertCypher);
    }

    @Test
    public void equalGraphsWithoutLabelsShouldPassSubgraphTest() {
        String assertCypher = "CREATE " +
                "(m {name:'Michal'})-[:WORKS_FOR]->(c {name:'GraphAware'})," +
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

    @Test
    public void singleNodeGraphsWithDifferentLabelsShouldFailSameGraphTest3() {
        populateDatabase("CREATE (m:Male {name:'Michal'}), (d:Female {name:'Daniela'})");

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, "CREATE (m:Female {name:'Michal'}), (d:Male {name:'Daniela'})");
        });
    }

    @Test
    public void singleNodeGraphsWithDifferentLabelsShouldFailSubgraphTest2() {
        populateDatabase("CREATE (m:Female {name:'Michal'}), (d:Male {name:'Daniela'})");

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, "CREATE (m:Male {name:'Michal'}), (d:Female {name:'Daniela'})");
        });
    }

    @Test
    public void singleNodeGraphsWithDifferentLabelsShouldFailSameGraphTest() {
        populateDatabase("CREATE (m:Person:Human {name:'Michal'})");

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, "CREATE (m:Person {name:'Michal'})");
        });
    }

    @Test
    public void singleNodeGraphsWithDifferentLabelsShouldFailSubgraphTest() {
        populateDatabase("CREATE (m:Person:Human {name:'Michal'})");

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, "CREATE (m:Person {name:'Michal'})");
        });
    }

    @Test
    public void singleNodeGraphsWithDifferentLabelsShouldFailSubgraphTest3() {
        populateDatabase("CREATE (m:Person:Human {name:'Michal'})");

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, "CREATE (m:Person:Developer {name:'Michal'})");
        });
    }

    @Test
    public void singleNodeGraphsWithDifferentLabelsShouldFailSameGraphTest2() {
        populateDatabase("CREATE (m:Person {name:'Michal'})");

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, "CREATE (m:Person:Human {name:'Michal'})");
        });
    }

    @Test
    public void extraRelationshipPropertyShouldFailSameGraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR {since:2014}]->(c)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void extraRelationshipPropertyShouldFailSubgraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR {since:2014}]->(c)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, assertCypher);
        });
    }

    @Test
    public void differentRelTypesShouldFailSameGraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR1]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR2]->(c)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR2]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR1]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void differentRelTypesShouldFailSubgraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR1]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR2]->(c)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR2]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR1]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, assertCypher);
        });
    }

    @Test
    public void extraRelationshipPropertyShouldFailSameGraphTest2() {
        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR {since:2014}]->(c)";

        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void missingRelationshipShouldFailSameGraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void missingRelationshipShouldFailSameGraphTest2() {
        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void missingRelationshipInDatabaseShouldFailSubgraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, assertCypher);
        });
    }

    @Test
    public void missingRelationshipInSubgraphShouldPassSubgraphTest() {
        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})";

        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void missingNodePropertyShouldFailSameGraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void missingNodePropertyShouldFailSubgraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, assertCypher);
        });
    }

    @Test
    public void correctSubgraphShouldPassSubgraphTest() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)," +
                "(:Person {name:'Adam'})-[:WORKS_FOR]->(c)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela', role:'Operations Director'})-[:WORKS_FOR]->(c)";

        populateDatabase(populateCypher);

        assertSubgraph(database, assertCypher);
    }

    @Test
    public void correctSubgraphShouldPassSubgraphTest2() {
        String populateCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c)," +
                "(:Person {name:'Adam'})-[:WORKS_FOR]->(c)," +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c2:Company {name:'GraphAware'})," +
                "(:Person {name:'Daniela'})-[:WORKS_FOR]->(c2)," +
                "(:Person {name:'Adam'})-[:WORKS_FOR]->(c2)";

        String assertCypher = "CREATE " +
                "(:Person {name:'Michal'})-[:WORKS_FOR]->(c:Company {name:'GraphAware'})," +
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

    @Test
    public void inCorrectSubgraphShouldFailSubgraphTest() {
        String populateCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String assertCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, assertCypher);
        });
    }

    @Test
    public void inCorrectSubgraphShouldFailSubgraphTest2() {
        String assertCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String populateCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSubgraph(database, assertCypher);
        });
    }

    @Test
    public void notEqualGraphsShouldFailTheSameGraphTest() {
        String populateCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String assertCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void notEqualGraphsShouldFailTheSameGraphTest2() {
        String assertCypher = "CREATE " +
                "(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(blue {name:'Blue'})<-[:REL]-(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        String populateCypher = "CREATE " +
                "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

        populateDatabase(populateCypher);

        assertThrows(AssertionError.class, () -> {
            assertSameGraph(database, assertCypher);
        });
    }

    @Test
    public void equalNumbersWithDifferentTypeShouldBeEqual() {
        try (Transaction tx = database.beginTx()) {
            tx.createNode().setProperty("number", 123);
            tx.commit();
        }

        String cypher = "CREATE (n {number:123})";

        assertSameGraph(database, cypher);
    }

    @Test
    public void equalArraysWithDifferentTypeShouldBeEqual() {
        try (Transaction tx = database.beginTx()) {
            tx.createNode().setProperty("number", new int[]{123, 124});
            tx.commit();
        }

        String cypher = "CREATE (n {number:[123,124]})";

        assertSameGraph(database, cypher);
    }

    @Test
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

        assertThrows(AssertionError.class, () -> {
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
        });
    }

    @Test
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

        assertThrows(AssertionError.class, () -> {
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
        });
    }

    @Test
    public void deletedRelationshipWithNewTypeShouldNotInfluenceEquality() { //bug test
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.createNode();
            Node node2 = tx.createNode();
            node1.createRelationshipTo(node2, RelationshipType.withName("ACCIDENT"));
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            tx.getAllRelationships().iterator().next().delete();
            tx.commit();
        }

        String cypher = "CREATE (n), (m)";

        assertSameGraph(database, cypher);
    }

    @Test
    public void deletedNewLabelShouldNotInfluenceEquality() { //bug test
        try (Transaction tx = database.beginTx()) {
            tx.createNode(Label.label("Accident"));
            tx.createNode(Label.label("RealLabel"));
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            tx.findNodes(Label.label("Accident")).forEachRemaining(node -> node.delete());
            tx.commit();
        }

        String cypher = "CREATE (n:RealLabel)";

        assertSameGraph(database, cypher);
    }

    @Test
    public void deletedNewPropsShouldNotInfluenceEquality() { //bug test
        long id;

        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.setProperty("accident", "dummy");
            id = node.getId();

            tx.createNode();
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            tx.getNodeById(id).delete();
            tx.commit();
        }

        String cypher = "CREATE (n)";

        assertSameGraph(database, cypher);
    }

    @Test
    public void clearGraphWithoutRuntimeShouldDeleteAllNodesAndRels() {
        try (Transaction tx = database.beginTx()) {
            String cypher = "CREATE " +
                    "(blue {name:'Blue'})<-[:REL]-(red1 {name:'Red'})-[:REL]->(black1 {name:'Black'})-[:REL]->(green {name:'Green'})," +
                    "(red2 {name:'Red'})-[:REL]->(black2 {name:'Black'})";

            populateDatabase(cypher);
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            clearGraph(tx);
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, count(tx.getAllNodes()));
            tx.commit();
        }

    }

    @Test
    public void clearGraphWithoutRuntimeShouldDeleteBasedOnNodeInclusionPolicy() {
        try (Transaction tx = database.beginTx()) {
            String cypher = "CREATE " +
                    "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                    "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";

            populateDatabase(cypher);
            tx.commit();
        }

        InclusionPolicies inclusionPolicies = InclusionPolicies.all().with(new BlueNodeInclusionPolicy());
        try (Transaction tx = database.beginTx()) {
            clearGraph(tx, inclusionPolicies);
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(5, count(tx.getAllNodes()));
            tx.commit();
        }

    }


    @Test
    public void clearGraphWithoutRuntimeShouldDeleteBasedOnRelInclusionPolicy() {
        try (Transaction tx = database.beginTx()) {
            String cypher = "CREATE " +
                    "(purple:Purple {name:'Purple'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                    "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'}), (blue1:Blue)-[:REL2]->(blue2:Blue)";

            populateDatabase(cypher);
            tx.commit();
        }

        InclusionPolicies inclusionPolicies = InclusionPolicies.all().with(new BlueNodeInclusionPolicy()).with(new Rel2InclusionPolicy());
        try (Transaction tx = database.beginTx()) {
            clearGraph(tx, inclusionPolicies);
            tx.commit();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(6, count(tx.getAllNodes()));
            assertEquals(4, count(tx.getAllRelationships()));
            tx.commit();
        }

    }

    @Test
    public void equalGraphsShouldPassSameGraphTestWithNodeRelInclusionPolicies() {
        String dbCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'}), (c1:ChangeSet)-[:NEXT]->(c2:ChangeSet)";
        populateDatabase(dbCypher);

        String assertCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";

        assertSameGraph(database, assertCypher, InclusionPolicies.all().with(new ExcludeChangeSetNodeInclusionPolicy()).with(new ExcludeNextInclusionPolicy()));
    }

    @Test
    public void equalGraphsShouldPassSameGraphTestWithPropertyInclusionPolicies() {
        String dbCypher = "CREATE " +
                "(blue:Blue {name:'Blue', createdOn: 12233322232})<-[:REL {count: 1}]-(red1:Red {name:'Red'})-[:REL {count:2}]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'}), (c1:ChangeSet)-[:NEXT]->(c2:ChangeSet)";
        populateDatabase(dbCypher);

        String assertCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";

        assertSameGraph(database, assertCypher, new InclusionPolicies(new ExcludeChangeSetNodeInclusionPolicy(), new ExcludeCreatedOnPropertyInclusionPolicy(), new ExcludeNextInclusionPolicy(), new ExcludeCountPropertyInclusionPolicy()));
    }


    @Test
    public void equalGraphsShouldPassSubgraphTestWithNodeRelInclusionPolicies() {
        String dbCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'}), (c1:ChangeSet)-[:NEXT]->(c2:ChangeSet)";
        populateDatabase(dbCypher);

        String assertCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";

        assertSubgraph(database, assertCypher, InclusionPolicies.all().with(new ExcludeChangeSetNodeInclusionPolicy()).with(new ExcludeNextInclusionPolicy()));
    }

    @Test
    public void equalGraphsShouldPassSubgraphTestWithPropertyInclusionPolicies() {
        String dbCypher = "CREATE " +
                "(blue:Blue {name:'Blue', createdOn: 12233322232})<-[:REL {count: 1}]-(red1:Red {name:'Red'})-[:REL {count:2}]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'}), (c1:ChangeSet)-[:NEXT]->(c2:ChangeSet)";
        populateDatabase(dbCypher);

        String assertCypher = "CREATE " +
                "(blue:Blue {name:'Blue'})<-[:REL]-(red1:Red {name:'Red'})-[:REL]->(black1:Black {name:'Black'})-[:REL]->(green:Green {name:'Green'})," +
                "(red2:Red {name:'Red'})-[:REL]->(black2:Black {name:'Black'})";

        assertSubgraph(database, assertCypher, new InclusionPolicies(new ExcludeChangeSetNodeInclusionPolicy(), new ExcludeCreatedOnPropertyInclusionPolicy(), new ExcludeNextInclusionPolicy(), new ExcludeCountPropertyInclusionPolicy()));
    }

    @Test
    public void shouldCorrectlyIdentifyEmptyDatabase() {
        assertEmpty(database);
        assertSameGraph(database, null);
        assertSameGraph(database, "");
        assertSameGraph(database, " ");
        assertSameGraph(database, "", InclusionPolicies.all());

        populateDatabase("CREATE (n:Person {name:'Michal'})");

        assertSameGraph(database, " ", InclusionPolicies.all().with(IncludeNoNodes.getInstance()));
    }

    @Test
    public void shouldCorrectlyIdentifyNotEmptyDatabase() {
        assertEmpty(database);
        populateDatabase("CREATE (n:Person {name:'Michal'})");
        assertNotEmpty(database);

        populateDatabase("MATCH (n) DETACH DELETE n");
        try {
            assertNotEmpty(database);
            fail("Should have failed");
        }
        catch (AssertionError e) {
            assertTrue(e.getMessage().startsWith("The database is empty with respect to inclusion policies:"));
        }
    }


    @Test
    public void shouldCorrectlyIdentifyEmptyDatabase2() {
        populateDatabase("CREATE (n:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'})");

        try {
            assertEmpty(database);
            fail();
        } catch (AssertionError e) {
            //ok
        }

        try {
            assertEmpty(database, InclusionPolicies.all());
            fail();
        } catch (AssertionError e) {
            //ok
        }

        try {
            assertEmpty(database, InclusionPolicies.all().with(new BaseNodeInclusionPolicy() {
                @Override
                public boolean include(Node node) {
                    return !node.hasLabel(Label.label("Person"));
                }
            }));
            fail();
        } catch (AssertionError e) {
            //ok
        }

        assertEmpty(database, InclusionPolicies.all().with(new BaseNodeInclusionPolicy() {
            @Override
            public boolean include(Node node) {
                return !node.hasLabel(Label.label("Person"));
            }
        }).with(new RelationshipInclusionPolicy.Adapter() {
            @Override
            public boolean include(Relationship relationship) {
                return !relationship.getStartNode().hasLabel(Label.label("Person")) && !relationship.getEndNode().hasLabel(Label.label("Person"));
            }
        }));
    }

    @Test
    public void shouldFailWhenWrongArgumentsPassed() {
        try {
            assertEmpty(null);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertEmpty(null, InclusionPolicies.all());
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSameGraph(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSameGraph(null, null, InclusionPolicies.all());
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSubgraph(database, null);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSubgraph(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSubgraph(null, null, InclusionPolicies.all());
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSubgraph(database, "");
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSubgraph(database, " ");
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            assertSubgraph(database, null, InclusionPolicies.all());
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            clearGraph(null);
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }

        try {
            clearGraph(null, InclusionPolicies.all());
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    @Test
    public void shouldPrintGraph() {
        populateDatabase("CREATE (n:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'})");
        printGraph(database);
        //not really testing output, just that this is not throwing errors.
    }

    /**
     * Include only nodes with label 'Blue'
     */
    class BlueNodeInclusionPolicy extends BaseNodeInclusionPolicy {

    @Override
        public boolean include(Node node) {
            return node.hasLabel(Label.label("Blue"));
        }
    }

    /**
     * Include everything except nodes labelled 'ChangeSet'
     */
    class ExcludeChangeSetNodeInclusionPolicy extends BaseNodeInclusionPolicy {

    @Override
        public boolean include(Node node) {
            return !(node.hasLabel(Label.label("ChangeSet")));
        }
    }

    /**
     * Include only relationships with type 'REL2'
     */
    class Rel2InclusionPolicy extends RelationshipInclusionPolicy.Adapter {

    @Override
        public boolean include(Relationship relationship) {
            return relationship.getType().name().equals("REL2");
        }
    }

    /**
     * Include everything except  relationships with type 'NEXT'
     */
    class ExcludeNextInclusionPolicy extends RelationshipInclusionPolicy.Adapter {

    @Override
        public boolean include(Relationship relationship) {
            return !(relationship.getType().name().equals("NEXT"));
        }
    }

    class ExcludeCountPropertyInclusionPolicy implements RelationshipPropertyInclusionPolicy {

    @Override
        public boolean include(String s, Relationship relationship) {
            return !s.equals("count");
        }
    }

    class ExcludeCreatedOnPropertyInclusionPolicy implements NodePropertyInclusionPolicy {

    @Override
        public boolean include(String s, Node node) {
            return !s.equals("createdOn");
        }
    }
}
