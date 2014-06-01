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

import com.graphaware.example.module.FriendshipStrengthCounter;
import com.graphaware.example.module.FriendshipStrengthModule;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.ProductionGraphAwareRuntime;
import com.graphaware.test.integration.ServerIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;


/**
 * Test for {@link com.graphaware.example.module.FriendshipStrengthCounter}.
 */
public class FriendshipStrengthModuleEmbeddedProgrammaticIntegrationTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        GraphAwareRuntime runtime = new ProductionGraphAwareRuntime(database);
        runtime.registerModule(new FriendshipStrengthModule("FSM", database));
    }

    @Test
    public void totalFriendshipStrengthOnEmptyDatabaseShouldBeZero() {
        assertEquals(0, new FriendshipStrengthCounter(database).getTotalFriendshipStrength());
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated() {
        new ExecutionEngine(database).execute("CREATE " +
                "(p1:Person)-[:FRIEND_OF {strength:2}]->(p2:Person)," +
                "(p1)-[:FRIEND_OF {strength:1}]->(p3:Person)");

        assertEquals(3, new FriendshipStrengthCounter(database).getTotalFriendshipStrength());
    }
}
