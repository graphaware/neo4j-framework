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

package com.graphaware.test;

import com.graphaware.tx.executor.single.SimpleTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import static com.graphaware.test.IterableUtils.*;
import static java.util.Arrays.asList;
import static junit.framework.Assert.*;

/**
 * Unit test for {@link com.graphaware.test.IterableUtils}.
 */
public class IterableUtilsTest {

    @Test
    public void newDatabaseShouldHaveOneNode() {
        assertEquals(1, countNodes(new TestGraphDatabaseFactory().newImpermanentDatabase()));
    }

    @Test
    public void afterCreatingANodeDatabaseShouldHaveTwoNodes() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                database.createNode();
                return null;
            }
        });

        assertEquals(2, countNodes(database));
    }

    @Test
    public void emptyDatabaseShouldHaveZeroNodes() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                database.getNodeById(0).delete();
                return null;
            }
        });

        assertEquals(0, countNodes(database));
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
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Node node = new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Node>() {
            @Override
            public Node doInTransaction(GraphDatabaseService database) {
                return database.createNode();
            }
        });

        Iterable<Node> nodes = GlobalGraphOperations.at(database).getAllNodes();

        assertTrue(contains(nodes, node));

        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                database.getNodeById(1).delete();
                return null;
            }
        });

        assertFalse(contains(nodes, node));
    }

    @Test
    public void testRandom() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        new SimpleTransactionExecutor(database).executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(GraphDatabaseService database) {
                database.createNode();
                return null;
            }
        });

        assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
        assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
    }

    @Test
    public void testRandomCollection() {
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
    }
}
