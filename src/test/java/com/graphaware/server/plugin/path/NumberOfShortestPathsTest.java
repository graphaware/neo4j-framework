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

package com.graphaware.server.plugin.path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link NumberOfShortestPaths}.
 */
public class NumberOfShortestPathsTest {

    private static final String COST = "cost";

    private enum RelTypes implements RelationshipType {
        R1, R2
    }

    private GraphDatabaseService database;
    private Node one;
    private Node three;

    private NumberOfShortestPaths plugin = new NumberOfShortestPaths();

    /**
     * Graph:
     * (1)-[:R2 {cost:1}]->(2)-[:R1 {cost:1}]->(3)
     * (1)-[:R1 {cost:1}]->(4)-[:R1 {cost:2}]->(5)-[:R1 {cost:1}]->(3)
     * (4)-[:R1 {cost:1}]->(2)
     */
    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        Transaction tx = database.beginTx();
        try {
            one = database.createNode();
            Node two = database.createNode();
            three = database.createNode();
            Node four = database.createNode();
            Node five = database.createNode();

            one.createRelationshipTo(two, RelTypes.R2).setProperty(COST, 1);
            two.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
            one.createRelationshipTo(four, RelTypes.R1).setProperty(COST, 1);
            four.createRelationshipTo(five, RelTypes.R1).setProperty(COST, 2);
            five.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
            four.createRelationshipTo(two, RelTypes.R1).setProperty(COST, 1);

            tx.success();
        } finally {
            tx.finish();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void noPathsShouldBeReturnedWhenThereIsNoPath() {
        assertEquals(0, Iterables.toList(plugin.paths(one, three, new String[]{"R2"}, 5, null, null)).size());
    }

    @Test
    public void noPathsShouldBeFoundWhenTraversalDepthIsTooSmall() {
        assertEquals(0, Iterables.toList(plugin.paths(one, three, new String[]{"R1"}, 2, 10, null)).size());
    }

    @Test
    public void allShortestPathsShouldBeReturned() {
        List<Path> paths = Iterables.toList(plugin.paths(one, three, new String[]{"R1"}, 3, 10, null));
        assertEquals(2, paths.size());
        assertEquals(3, paths.get(0).length());
        assertEquals(3, paths.get(1).length());

        paths = Iterables.toList(plugin.paths(one, three, new String[]{"R1", "R2"}, 3, 10, null));
        assertEquals(2, paths.size());
        assertEquals(2, paths.get(0).length());
        assertEquals(3, paths.get(1).length());

        paths = Iterables.toList(plugin.paths(one, three, null, 3, 10, null));
        assertEquals(2, paths.size());
        assertEquals(2, paths.get(0).length());
        assertEquals(3, paths.get(1).length());
    }

    @Test
    public void shouldLimitPaths() {
        List<Path> paths = Iterables.toList(plugin.paths(one, three, null, 3, 1, null));
        assertEquals(1, paths.size());
        assertEquals(2, paths.get(0).length());
    }

    /**
     * Graph:
     * (1)-[:R2 {cost:1}]->(2)-[:R1 {cost:1}]->(3)
     * (1)-[:R1 {cost:1}]->(4)-[:R1 {cost:2}]->(5)-[:R1 {cost:1}]->(3)
     * (4)-[:R1 {cost:1}]->(2)
     * (1)-[:R1 {cost:1}]->(6)-[:R2 {cost:N/A}]-(7)-[:R1 {cost:1}]->(3)
     * (1)-[:R1 {cost:1}]->(8)-[:R2 {cost:1}]-(9)-[:R1 {cost:1}]->(3)
     */
    @Test
    public void shouldCorrectlyOrderPaths() {
        Transaction tx = database.beginTx();
        try {
            Node six = database.createNode();
            Node seven = database.createNode();
            Node eight = database.createNode();
            Node nine = database.createNode();

            one.createRelationshipTo(six, RelTypes.R1).setProperty(COST, 1);
            six.createRelationshipTo(seven, RelTypes.R2);
            seven.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);

            one.createRelationshipTo(eight, RelTypes.R1).setProperty(COST, 1);
            eight.createRelationshipTo(nine, RelTypes.R2).setProperty(COST, 1);
            nine.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);

            tx.success();
        } finally {
            tx.finish();
        }

        List<Path> paths = Iterables.toList(plugin.paths(one, three, null, null, null, COST));
        assertEquals(4, paths.size());
        assertEquals(2, paths.get(0).length());
        assertEquals(3, paths.get(1).length());
        assertEquals(3, paths.get(2).length());
        assertEquals(3, paths.get(3).length());

        for (Path p : paths) {
            System.out.println(p.toString());
        }

        assertEquals(8, Iterables.toList(paths.get(1).nodes()).get(1).getId());
        assertEquals(4, Iterables.toList(paths.get(2).nodes()).get(1).getId());
        assertEquals(6, Iterables.toList(paths.get(3).nodes()).get(1).getId());
    }
}
