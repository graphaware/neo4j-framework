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
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.common.util.IterableUtils.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.common.util.IterableUtils}.
 */
public class IterableUtilsTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);
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
            assertTrue(contains(database.getAllNodes(), node));
        }

        try (Transaction tx = database.beginTx()) {
            database.getNodeById(0).delete();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertFalse(contains(database.getAllNodes(), node));
        }
    }

    @Test
    public void testRandom() {
        try (Transaction tx = database.beginTx()) {
            database.createNode();
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
            assertTrue(asList(0L, 1L).contains(random(database.getAllNodes()).getId()));
        }
    }

    @Test
    public void testRandomCollection() {
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
        assertTrue(asList(0L, 1L).contains(random(asList(0L, 1L))));
    }

    @Test
    public void singleElementShouldBeReturnedWhenIterableHasOneElement() {
        assertEquals("test", getSingleOrNull(Collections.singletonList("test")));
    }

    @Test
    public void singleElementShouldBeReturnedWhenIterableHasOneElement2() {
        assertEquals("test", getSingle(Collections.singletonList("test")));
    }

    @Test
    public void nullShouldBeReturnedWhenIterableHasNoElements() {
        assertNull(getSingleOrNull(Collections.emptyList()));
    }

    @Test(expected = NotFoundException.class)
    public void exceptionShouldBeThrownWhenIterableHasNoElements() {
        getSingle(Collections.emptyList());
    }

    @Test
    public void exceptionShouldBeThrownWhenIterableHasNoElements2() {
        try {
            getSingle(Collections.emptyList(), "test");
        } catch (NotFoundException e) {
            assertEquals("test", e.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionShouldBeThrownWhenIterableHasMoreThanOneElement() {
        getSingleOrNull(Arrays.asList("test1", "test2"));
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionShouldBeThrownWhenIterableHasMoreThanOneElement2() {
        getSingle(Arrays.asList("test1", "test2"));
    }

    //

    @Test
    public void firstElementShouldBeReturnedWhenIterableHasOneElement() {
        assertEquals("test", getFirstOrNull(Collections.singletonList("test")));
    }

    @Test
    public void firstElementShouldBeReturnedWhenIterableHasOneElement2() {
        assertEquals("test", getFirst(Collections.singletonList("test"), "test"));
    }

    @Test
    public void nullShouldBeReturnedWhenIterableHasNoElementsWhenRequestingFirst() {
        assertNull(getFirstOrNull(Collections.emptyList()));
    }

    @Test(expected = NotFoundException.class)
    public void exceptionShouldBeThrownWhenIterableHasNoElementsWhenRequestingFirst() {
        getFirst(Collections.emptyList(), "test");
    }

    @Test
    public void exceptionShouldBeThrownWhenIterableHasNoElements2WhenRequestingFirst() {
        try {
            getFirst(Collections.emptyList(), "test");
        } catch (NotFoundException e) {
            assertEquals("test", e.getMessage());
        }
    }

    @Test
    public void shouldReturnFirstWhenThereIsMoreThanOne() {
        assertEquals("test1", getFirstOrNull(Arrays.asList("test1", "test2")));
    }

    @Test
    public void exceptionShouldBeThrownWhenIterableHasMoreThanOneElement2WhenRequestingFirst() {
        assertEquals("test1", getFirst(Arrays.asList("test1", "test2"), "test"));
    }


    @Test
    public void shouldSampleIterable() {
        Iterable<Long> sampled = random(asList(1L, 2L, 3L, 4L, 5L), 2);
        assertEquals(2, count(sampled));
        List<Long> longs = toList(sampled);
        assertTrue(longs.contains(1L) || longs.contains(2L) || longs.contains(3L) || longs.contains(4L) || longs.contains(5L));
    }

    @Test
    public void randomSelectionShouldBeUniform() {
        int noSamples = 100;
        int noTrials = 1000000;
        double tolerance = 0.05;

        List<Integer> input = new LinkedList<>();
        for (int i = 0; i < noSamples; i++) {
            input.add(i);
        }

        Counter<Integer> counter = new Counter<>();
        for (int i = 0; i < noTrials; i++) {
            counter.increment(IterableUtils.random(input));
        }

        for (int i = 0; i < noSamples; i++) {
            long count = counter.getCount(i);
            double lowerBound = (noTrials / noSamples) * (1 - tolerance);
            double upperBound = (noTrials / noSamples) * (1 + tolerance);
            assertTrue(count > lowerBound);
            assertTrue(count < upperBound);
        }
    }
}
