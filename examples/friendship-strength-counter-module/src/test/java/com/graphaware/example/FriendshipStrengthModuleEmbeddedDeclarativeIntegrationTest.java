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
import com.graphaware.runtime.bootstrap.RuntimeExtensionFactory;
import com.graphaware.runtime.settings.FrameworkSettingsDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test for {@link com.graphaware.example.module.FriendshipStrengthCounter}.
 */
public class FriendshipStrengthModuleEmbeddedDeclarativeIntegrationTest {

    private Neo4j neo4j;
    private GraphDatabaseService database;

    @BeforeEach
    public void setUp() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withExtensionFactories(new ArrayList<>(Collections.singleton(new RuntimeExtensionFactory())))
                .withConfig(FrameworkSettingsDeclaration.ga_config_file_name, "neo4j-friendship.conf")
                .build();

        database = neo4j.defaultDatabaseService();
    }

    @AfterEach
    public void tearDown() {
        neo4j.close();
    }

    @Test
    public void totalFriendshipStrengthOnEmptyDatabaseShouldBeZero() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new FriendshipStrengthCounter().getTotalFriendshipStrength(tx));
            tx.commit();
        }
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated() {
        database.executeTransactionally("CREATE " +
                "(p1:Person)-[:FRIEND_OF {strength:2}]->(p2:Person)," +
                "(p1)-[:FRIEND_OF {strength:1}]->(p3:Person)");

        try (Transaction tx = database.beginTx()) {
            assertEquals(3, new FriendshipStrengthCounter().getTotalFriendshipStrength(tx));
            tx.commit();
        }
    }
}
