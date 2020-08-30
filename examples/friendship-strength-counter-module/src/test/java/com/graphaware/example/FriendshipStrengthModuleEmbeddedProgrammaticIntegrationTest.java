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

import com.graphaware.example.module.FriendshipStrengthCounter;
import com.graphaware.example.module.FriendshipStrengthModule;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;
import org.neo4j.harness.TestServerBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test for {@link com.graphaware.example.module.FriendshipStrengthCounter}.
 */
public class FriendshipStrengthModuleEmbeddedProgrammaticIntegrationTest {

    private GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        database = TestServerBuilders.newInProcessBuilder().newServer().graph();

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);
        runtime.registerModule(new FriendshipStrengthModule("FSM", database));
        runtime.start();
    }

    @AfterEach
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void totalFriendshipStrengthOnEmptyDatabaseShouldBeZero() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new FriendshipStrengthCounter(database).getTotalFriendshipStrength());
            tx.success();
        }
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated() {
        database.execute("CREATE " +
                "(p1:Person)-[:FRIEND_OF {strength:2}]->(p2:Person)," +
                "(p1)-[:FRIEND_OF {strength:1}]->(p3:Person)");

        try (Transaction tx = database.beginTx()) {
            assertEquals(3, new FriendshipStrengthCounter(database).getTotalFriendshipStrength());
            tx.success();
        }
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated2() {
        try (Transaction tx = database.beginTx()) {
            Node p1 = database.createNode(Label.label("Person"));
            Node p2 = database.createNode(Label.label("Person"));
            Node p3 = database.createNode(Label.label("Person"));
            p1.createRelationshipTo(p2, RelationshipType.withName("FRIEND_OF")).setProperty("strength", 1L);
            p1.createRelationshipTo(p3, RelationshipType.withName("FRIEND_OF")).setProperty("strength", 2L);
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(3, new FriendshipStrengthCounter(database).getTotalFriendshipStrength());
            tx.success();
        }
    }
}
