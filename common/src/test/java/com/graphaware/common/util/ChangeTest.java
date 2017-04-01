/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.common.util.Change.changesToMap;
import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 *  Unit test for {@link Change}.
 */
public class ChangeTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);

        database.execute("CREATE " +
                "(a), " +
                "(b {key:'value'})," +
                "(b)-[:test]->(a)," +
                "(c {key:'value'})");
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldConvertChangesToMap() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange = new Change<>(database.getNodeById(0), database.getNodeById(0));
            Map<Long, Change<Node>> changeMap = changesToMap(asList(nodeChange));
            assertEquals(0, changeMap.get(0L).getCurrent().getId());
            assertEquals(0, changeMap.get(0L).getPrevious().getId());
            assertEquals(1, changeMap.size());
        }
    }

    @Test
    public void equalChangesShouldBeEqual() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange1 = new Change<>(database.getNodeById(0), database.getNodeById(0));
            Change<Node> nodeChange2 = new Change<>(database.getNodeById(0), database.getNodeById(0));
            Change<Node> nodeChange3 = new Change<>(database.getNodeById(1), database.getNodeById(1));

            assertTrue(nodeChange1.equals(nodeChange2));
            assertTrue(nodeChange2.equals(nodeChange1));
            assertFalse(nodeChange3.equals(nodeChange1));
            assertFalse(nodeChange1.equals(nodeChange3));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidChangeShouldThrowException() {
        try (Transaction tx = database.beginTx()) {
            Change<Node> nodeChange1 = new Change<>(database.getNodeById(0), database.getNodeById(1));
            changesToMap(Collections.singleton(nodeChange1));
        }
    }
}
