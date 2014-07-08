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

import com.graphaware.test.integration.NeoServerIntegrationTest;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static com.graphaware.test.util.TestUtils.executeCypher;
import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;


/**
 * Test for {@link com.graphaware.example.module.FriendshipStrengthCounter}.
 */
public class FriendshipStrengthModuleServerIntegrationTest extends NeoServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String neo4jConfigFile() {
        return "neo4j-friendship.properties";
    }

    @Test
    public void totalFriendshipStrengthOnEmptyDatabaseShouldBeZero() {
         assertEquals("0", get(baseUrl()+"/graphaware/friendship/strength", HttpStatus.SC_OK));
    }

    @Test
    public void totalFriendshipStrengthShouldBeCorrectlyCalculated() {
        executeCypher(baseUrl(),
                "CREATE " +
                "(p1:Person)-[:FRIEND_OF {strength:2}]->(p2:Person)," +
                "(p1)-[:FRIEND_OF {strength:1}]->(p3:Person)");

        assertEquals("3", get(baseUrl()+"/graphaware/friendship/strength", HttpStatus.SC_OK));
    }
}
