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

package com.graphaware.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Map;

import static com.graphaware.example.FriendshipStrengthCounter.*;
import static org.junit.Assert.assertEquals;


/**
 * Test for {@link FriendshipStrengthCounter}.
 */
public class FriendshipStrengthCounterTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        database.registerTransactionEventHandler(new FriendshipStrengthCounter(database));
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void totalFriendshipStrengthShouldBeZeroInEmptyDatabase() {
        assertEquals(0L, getTotalFriendshipStrength(database));
    }

    @Test
    public void totalFriendshipStrengthShouldBeCounted() {
        try (Transaction tx = database.beginTx()) {
            Node person1 = database.createNode();
            Node person2 = database.createNode();
            Node person3 = database.createNode();

            person1.setProperty("name", "Person One");
            person2.setProperty("name", "Person Two");
            person3.setProperty("name", "Person Three");

            person1.createRelationshipTo(person2, FRIEND_OF).setProperty(STRENGTH, 2L);
            person1.createRelationshipTo(person3, FRIEND_OF).setProperty(STRENGTH, 1L);
            person2.createRelationshipTo(person1, FRIEND_OF).setProperty(STRENGTH, 3L);
            person2.createRelationshipTo(person3, FRIEND_OF).setProperty(STRENGTH, 1L);
            person3.createRelationshipTo(person1, FRIEND_OF).setProperty(STRENGTH, 2L);

            tx.success();
        }

        assertEquals(9L, getTotalFriendshipStrength(database));

        //delete and change some friendships
        try (Transaction tx = database.beginTx()) {
            for (Relationship relationship : database.getNodeById(1).getRelationships(FRIEND_OF, Direction.OUTGOING)) {
                if (relationship.getEndNode().getId() == 2) {
                    relationship.delete(); //remove 2 from total strength
                } else {
                    relationship.setProperty(STRENGTH, 2L); //add 1 to total strength
                }
            }

            tx.success();
        }

        assertEquals(8L, getTotalFriendshipStrength(database));
    }

    @Test
    public void totalFriendshipStrengthShouldBeCountedUsingCypher() {
        ExecutionEngine executionEngine = new ExecutionEngine(database);

        executionEngine.execute("CREATE " +
                "(p1:Person), (p2:Person), (p3:Person)," +
                "(p1)-[:FRIEND_OF {strength:3}]->(p2)," +
                "(p2)-[:FRIEND_OF {strength:1}]->(p1)," +
                "(p1)-[:FRIEND_OF {strength:2}]->(p3)");

        String query = "MATCH (c:FriendshipCounter) RETURN c.totalFriendshipStrength as result";

        for (Map<String, Object> result : executionEngine.execute(query)) {
            assertEquals(6L, result.get("result"));
        }

        executionEngine.execute("MATCH (p1:Person)-[f:FRIEND_OF {strength:3}]->(p2) DELETE f");

        for (Map<String, Object> result : executionEngine.execute(query)) {
            assertEquals(3L, result.get("result"));
        }
    }
}
