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

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test for {@link com.graphaware.example.module.FriendshipStrengthCounter}.
 */
public class FriendshipStrengthModuleServerIntegrationTest extends GraphAwareIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String configFile() {
        return "neo4j-friendship.conf";
    }

    @Test
    public void totalFriendshipStrengthOnEmptyDatabaseShouldBeZero() {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n:FriendshipCounter) RETURN n.totalFriendshipStrength");
            assertFalse(result.hasNext());
        }
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated() {
        try (Session session = driver.session()) {
            session.run("CREATE " +
                    "(p1:Person)-[:FRIEND_OF {strength:2}]->(p2:Person)," +
                    "(p1)-[:FRIEND_OF {strength:1}]->(p3:Person)");
        }

        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n:FriendshipCounter) RETURN n.totalFriendshipStrength");
            assertTrue(result.hasNext());
            assertEquals(3L, result.next().get(0).asNumber());
        }
    }
}
