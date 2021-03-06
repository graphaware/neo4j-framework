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

package com.graphaware.example;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;

import java.util.Iterator;
import java.util.Map;

import static com.graphaware.example.FriendshipStrengthCounter.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test for {@link FriendshipStrengthCounter}.
 */
@ExtendWith(Neo4jExtension.class)
public class FriendshipStrengthCounterTest {

    @InjectNeo4j
    private Neo4j neo4j;
    @InjectNeo4j
    private GraphDatabaseService database;

    private FriendshipStrengthCounter listener;

    @BeforeEach
    public void setUp() throws Exception {
        listener = new FriendshipStrengthCounter(database);
        neo4j.databaseManagementService().registerTransactionEventListener(database.databaseName(), listener);
    }

    @AfterEach
    public void tearDown() {
        neo4j.databaseManagementService().unregisterTransactionEventListener(database.databaseName(), listener);
    }

    @Test
    public void totalFriendshipStrengthShouldBeZeroInEmptyDatabase() {
        assertEquals(0L, getTotalFriendshipStrength(database));
    }

    @Test
    public void totalFriendshipStrengthShouldBeCounted() {
        long p1id, p2id;

        try (Transaction tx = database.beginTx()) {
            Node person1 = tx.createNode();
            Node person2 = tx.createNode();
            Node person3 = tx.createNode();

            p1id = person1.getId();
            p2id = person2.getId();

            person1.setProperty("name", "Person One");
            person2.setProperty("name", "Person Two");
            person3.setProperty("name", "Person Three");

            person1.createRelationshipTo(person2, FRIEND_OF).setProperty(STRENGTH, 2L);
            person1.createRelationshipTo(person3, FRIEND_OF).setProperty(STRENGTH, 1L);
            person2.createRelationshipTo(person1, FRIEND_OF).setProperty(STRENGTH, 3L);
            person2.createRelationshipTo(person3, FRIEND_OF).setProperty(STRENGTH, 1L);
            person3.createRelationshipTo(person1, FRIEND_OF).setProperty(STRENGTH, 2L);

            tx.commit();
        }

        assertEquals(9L, getTotalFriendshipStrength(database));

        //delete and change some friendships
        try (Transaction tx = database.beginTx()) {
            for (Relationship relationship : tx.getNodeById(p1id).getRelationships(Direction.OUTGOING, FRIEND_OF)) {
                if (relationship.getEndNode().getId() == p2id) {
                    relationship.delete(); //remove 2 from total strength
                } else {
                    relationship.setProperty(STRENGTH, 2L); //add 1 to total strength
                }
            }

            tx.commit();
        }

        assertEquals(8L, getTotalFriendshipStrength(database));
    }

    @Test
    public void totalFriendshipStrengthShouldBeCountedUsingCypher() {
        database.executeTransactionally("CREATE " +
                "(p1:Person), (p2:Person), (p3:Person)," +
                "(p1)-[:FRIEND_OF {strength:3}]->(p2)," +
                "(p2)-[:FRIEND_OF {strength:1}]->(p1)," +
                "(p1)-[:FRIEND_OF {strength:2}]->(p3)");

        String query = "MATCH (c:FriendshipCounter) RETURN c.totalFriendshipStrength as result";

        try (Transaction tx = database.beginTx()) {
            Iterator<Map<String, Object>> execute = tx.execute(query);

            while (execute.hasNext()) {
                Map<String, Object> result = execute.next();
                assertEquals(6L, result.get("result"));
            }

            tx.commit();
        }

        query = "MATCH (p1:Person)-[f:FRIEND_OF {strength:3}]->(p2) DELETE f";

        try (Transaction tx = database.beginTx()) {
            Iterator<Map<String, Object>> execute = tx.execute(query);

            while (execute.hasNext()) {
                Map<String, Object> result = execute.next();
                assertEquals(3L, result.get("result"));
            }

            tx.commit();
        }
    }
}
