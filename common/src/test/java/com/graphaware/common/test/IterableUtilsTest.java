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

package com.graphaware.common.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.test.IterableUtils.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Unit test for {@link IterableUtils}.
 */
public class IterableUtilsTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void newDatabaseShouldHaveNoNodes() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0, countNodes(database));
        }
    }

    @Test
    public void afterCreatingANodeDatabaseShouldHaveOneNode() {
        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(1, countNodes(database));
        }
    }

    @Test
    public void listWithOneItemShouldHaveOneItem() {
        assertEquals(1, count(asList("test")));
    }

    @Test
    public void checkContainsCollections() {
        assertTrue(contains(asList("a", "b"), "b"));
        assertFalse(contains(asList("a", "b"), "c"));
    }

    @Test
    public void checkContainsRealIterables() {
        Node node;

        try (Transaction tx = database.beginTx()) {
            node = database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertTrue(contains(at(database).getAllNodes(), node));
        }

        try (Transaction tx = database.beginTx()) {
            database.getNodeById(0).delete();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertFalse(contains(at(database).getAllNodes(), node));
        }
    }

    @Test
    public void testRandom() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertTrue(asList(0L, 1L).contains(random(at(database).getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(at(database).getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(at(database).getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(at(database).getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(at(database).getAllNodes()).getId()));
        }
    }

    @Test
    public void testRandomCollection() {
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
    }
}
