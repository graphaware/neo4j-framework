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

package com.graphaware.tx.event.improved;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.event.improved.api.LazyTransactionData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListenerAdapter;
import org.neo4j.harness.Neo4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.graphdb.Label.label;

/**
 * Unit test for {@link com.graphaware.tx.event.improved.api.LazyTransactionData}.
 */
@ExtendWith(Neo4jExtension.class)
public class LazyTransactionDataSmokeTest {

    @InjectNeo4j
    private Neo4j neo4j;
    @InjectNeo4j
    private GraphDatabaseService database;

    private CapturingTransactionEventHandler eventHandler;

    @BeforeEach
    public void setUp() {
        eventHandler = new CapturingTransactionEventHandler();
        neo4j.databaseManagementService().registerTransactionEventListener(neo4j.defaultDatabaseService().databaseName(), eventHandler);
    }

    @AfterEach
    public void tearDown() {
        neo4j.databaseManagementService().unregisterTransactionEventListener(neo4j.defaultDatabaseService().databaseName(), eventHandler);
    }

    @Test
    public void nothingShouldBeReportedWhenNoChangesOccur() {
        try (Transaction tx = database.beginTx()) {
            Node n = tx.createNode(label("TestLabel"));
            tx.getNodeById(n.getId()).delete();
            tx.commit();
        }

        verify(Collections.<String>emptySet());
    }

    @Test
    public void createdNodesShouldBePickedUp() {
        execute("CREATE (p:Person {name:'Michal'})");
        verify("Created node (:Person {name: Michal})");
    }

    @Test
    public void nodesWithChangedLabelsShouldBePickedUp() {
        execute("CREATE (p:Person {name:'Michal'})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) SET p:NewLabel");
        verify("Changed node (:Person {name: Michal}) to (:NewLabel:Person {name: Michal})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) REMOVE p:NewLabel");
        verify("Changed node (:NewLabel:Person {name: Michal}) to (:Person {name: Michal})");
    }

    @Test
    public void nodesWithChangedPropertiesShouldBePickedUp() {
        execute("CREATE (p:Person {name:'Michal'})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) SET p.age=30");
        verify("Changed node (:Person {name: Michal}) to (:Person {age: 30, name: Michal})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) REMOVE p.age");
        verify("Changed node (:Person {age: 30, name: Michal}) to (:Person {name: Michal})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) SET p.name='Peter'");
        verify("Changed node (:Person {name: Michal}) to (:Person {name: Peter})");
    }

    @Test
    public void nodesWithPropertiesThatStayedTheSameShouldNotBePickedUp() {
        execute("CREATE (p:Person {name:'Michal'})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) SET p.name='Michal'");
        verify(Collections.<String>emptySet());
    }

    @Test
    public void nodesWithChangedLabelsAndPropertiesShouldBePickedUp() {
        execute("CREATE (p:Person {name:'Michal'})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) SET p.name='Peter', p:NewLabel");
        verify("Changed node (:Person {name: Michal}) to (:NewLabel:Person {name: Peter})");
    }

    @Test
    public void deletedNodesShouldBePickedUp() {
        execute("CREATE (p:Person {name:'Michal'})");

        startCapturing();
        execute("MATCH (p:Person {name:'Michal'}) DELETE p");
        verify("Deleted node (:Person {name: Michal})");
    }

    @Test
    public void createdRelationshipShouldBePickedUp() {
        execute("CREATE (:Person {name:'Michal'})-[:FRIEND_OF {since:2007}]->(:Person {name:'Daniela'})");

        verify(
                "Created node (:Person {name: Michal})",
                "Created node (:Person {name: Daniela})",
                "Created relationship (:Person {name: Michal})-[:FRIEND_OF {since: 2007}]->(:Person {name: Daniela})"
        );
    }

    @Test
    public void changedRelationshipShouldBePickedUp() {
        execute("CREATE (:Person {name:'Michal'})-[:FRIEND_OF {since:2007}]->(:Person {name:'Daniela'})");

        startCapturing();
        execute("MATCH (:Person {name:'Michal'})-[r:FRIEND_OF]->(:Person {name:'Daniela'}) SET r.since=2008");
        verify("Changed relationship (:Person {name: Michal})-[:FRIEND_OF {since: 2007}]->(:Person {name: Daniela})" +
                " to (:Person {name: Michal})-[:FRIEND_OF {since: 2008}]->(:Person {name: Daniela})");

        startCapturing();
        execute("MATCH (:Person {name:'Michal'})-[r:FRIEND_OF]->(:Person {name:'Daniela'}) REMOVE r.since");
        verify("Changed relationship (:Person {name: Michal})-[:FRIEND_OF {since: 2008}]->(:Person {name: Daniela})" +
                " to (:Person {name: Michal})-[:FRIEND_OF]->(:Person {name: Daniela})");

        startCapturing();
        execute("MATCH (:Person {name:'Michal'})-[r:FRIEND_OF]->(:Person {name:'Daniela'}) SET r.since=2006");
        verify("Changed relationship (:Person {name: Michal})-[:FRIEND_OF]->(:Person {name: Daniela})" +
                " to (:Person {name: Michal})-[:FRIEND_OF {since: 2006}]->(:Person {name: Daniela})");
    }

    @Test
    public void deletedRelationshipShouldBePickedUp() {
        execute("CREATE (:Person {name:'Michal'})-[:FRIEND_OF {since:2007}]->(:Person {name:'Daniela'})");

        startCapturing();
        execute("MATCH (:Person {name:'Michal'})-[r:FRIEND_OF]->(:Person {name:'Daniela'}) DELETE r");
        verify("Deleted relationship (:Person {name: Michal})-[:FRIEND_OF {since: 2007}]->(:Person {name: Daniela})");
    }

    @Test
    public void multipleChangesShouldBeCorrectlyPickedUp() {
        execute("CREATE (:Person {name:'Michal'})-[:FRIEND_OF {since:2007}]->(:Person {name:'Daniela'})");

        startCapturing();
        execute("MATCH (p1:Person {name:'Michal'})-[r:FRIEND_OF {since:2007}]->(p2:Person {name:'Daniela'}) DELETE r, p1, p2");
        verify(
                "Deleted relationship (:Person {name: Michal})-[:FRIEND_OF {since: 2007}]->(:Person {name: Daniela})",
                "Deleted node (:Person {name: Michal})",
                "Deleted node (:Person {name: Daniela})"
        );
    }

    @Test
    public void multipleChangesShouldBeCorrectlyPickedUp2() {
        execute("CREATE ({name:'Michal'})-[:FRIEND_OF {since:2007}]->(:Person {name:'Daniela'})");

        startCapturing();
        execute("MATCH (p1 {name:'Michal'})-[r:FRIEND_OF {since:2007}]->(p2:Person {name:'Daniela'}) DELETE r, p1, p2");
        verify("Deleted relationship ({name: Michal})-[:FRIEND_OF {since: 2007}]->(:Person {name: Daniela})",
                "Deleted node ({name: Michal})",
                "Deleted node (:Person {name: Daniela})"
        );
    }

    private void execute(String cypher) {
        database.executeTransactionally(cypher);
    }

    private void startCapturing() {
        eventHandler.clearCapturedData();
    }

    private void verify(String... expected) {
        Set<String> expectedSet = new HashSet<>();
        Collections.addAll(expectedSet, expected);
        verify(expectedSet);
    }

    private void verify(Set<String> expected) {
        verify(expected, eventHandler.getCapturedData());
    }

    private void verify(Set<String> expected, Set<String> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }

    private class CapturingTransactionEventHandler extends TransactionEventListenerAdapter<Void> {

        private Set<String> capturedData = new HashSet<>();

        @Override
        public Void beforeCommit(TransactionData data, Transaction tx, GraphDatabaseService database) {
            ImprovedTransactionData improvedTransactionData = new LazyTransactionData(data, tx);
            capturedData.addAll(improvedTransactionData.mutationsToStrings());
            return null;
        }

        public Set<String> getCapturedData() {
            return capturedData;
        }

        public void clearCapturedData() {
            capturedData.clear();
        }
    }
}
