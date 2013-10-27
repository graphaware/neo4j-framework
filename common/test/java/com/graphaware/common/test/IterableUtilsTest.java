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

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Unit test for {@link IterableUtils}.
 */
public class IterableUtilsTest {

    @Test
    public void newDatabaseShouldHaveOneNode() {
        assertEquals(1, IterableUtils.countNodes(new TestGraphDatabaseFactory().newImpermanentDatabase()));
    }

    @Test
    public void afterCreatingANodeDatabaseShouldHaveTwoNodes() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        
        Transaction tx = database.beginTx();
        try {
            database.createNode();
            tx.success();
        } finally {
            tx.finish();
        }

        assertEquals(2, IterableUtils.countNodes(database));
    }

    @Test
    public void emptyDatabaseShouldHaveZeroNodes() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        Transaction tx = database.beginTx();
        try {
            database.getNodeById(0).delete();
            tx.success();
        } finally {
            tx.finish();
        }

        assertEquals(0, IterableUtils.countNodes(database));
    }

    @Test
    public void listWithOneItemShouldHaveOneItem() {
        assertEquals(1, IterableUtils.count(asList("test")));
    }

    @Test
    public void checkContainsCollections() {
        assertTrue(IterableUtils.contains(asList("a", "b"), "b"));
        assertFalse(IterableUtils.contains(asList("a", "b"), "c"));
    }

    @Test
    public void checkContainsRealIterables() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Node node;

        Transaction tx = database.beginTx();
        try {
            node = database.createNode();
            tx.success();
        } finally {
            tx.finish();
        }

        Iterable<Node> nodes = GlobalGraphOperations.at(database).getAllNodes();

        assertTrue(IterableUtils.contains(nodes, node));

        tx = database.beginTx();
        try {
            database.getNodeById(1).delete();
            tx.success();
        } finally {
            tx.finish();
        }

        assertFalse(IterableUtils.contains(nodes, node));
    }

    @Test
    public void testRandom() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        Transaction tx = database.beginTx();
        try {
            database.createNode();
            tx.success();
        } finally {
            tx.finish();
        }

        assertTrue(asList(0L, 1L).contains(IterableUtils.random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(IterableUtils.random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(IterableUtils.random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(IterableUtils.random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(IterableUtils.random(database.getAllNodes()).getId()));
    }

    @Test
    public void testRandomCollection() {
        assertTrue(asList(0L, 1L).contains(IterableUtils.random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(IterableUtils.random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(IterableUtils.random(asList(0L, 1L))));
    }
}
