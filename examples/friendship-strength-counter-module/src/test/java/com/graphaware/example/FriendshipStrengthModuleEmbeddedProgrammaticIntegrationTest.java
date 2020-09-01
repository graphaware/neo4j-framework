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
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test for {@link com.graphaware.example.module.FriendshipStrengthCounter}.
 */
public class FriendshipStrengthModuleEmbeddedProgrammaticIntegrationTest {

    private Neo4j database;

    @BeforeEach
    public void setUp() {
        database = Neo4jBuilders.newInProcessBuilder().build();

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database.databaseManagementService(), database.defaultDatabaseService());
        runtime.registerModule(new FriendshipStrengthModule("FSM", database.defaultDatabaseService()));
        runtime.start();
    }

    @AfterEach
    public void tearDown() {
        database.close();
    }

    @Test
    public void totalFriendshipStrengthOnEmptyDatabaseShouldBeZero() {
        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            assertEquals(0, new FriendshipStrengthCounter().getTotalFriendshipStrength(tx));
            tx.commit();
        }
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated() {
        database.defaultDatabaseService().executeTransactionally("CREATE " +
                "(p1:Person)-[:FRIEND_OF {strength:2}]->(p2:Person)," +
                "(p1)-[:FRIEND_OF {strength:1}]->(p3:Person)");

        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            assertEquals(3, new FriendshipStrengthCounter().getTotalFriendshipStrength(tx));
            tx.commit();
        }
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated2() {
        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            Node p1 = tx.createNode(Label.label("Person"));
            Node p2 = tx.createNode(Label.label("Person"));
            Node p3 = tx.createNode(Label.label("Person"));
            p1.createRelationshipTo(p2, RelationshipType.withName("FRIEND_OF")).setProperty("strength", 1L);
            p1.createRelationshipTo(p3, RelationshipType.withName("FRIEND_OF")).setProperty("strength", 2L);
            tx.commit();
        }

        try (Transaction tx = database.defaultDatabaseService().beginTx()) {
            assertEquals(3, new FriendshipStrengthCounter().getTotalFriendshipStrength(tx));
            tx.commit();
        }
    }
}
