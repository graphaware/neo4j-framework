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

package com.graphaware.api.library.algo.path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.BOTH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.kernel.Traversal.pathExpanderForAllTypes;
import static org.neo4j.kernel.Traversal.pathExpanderForTypes;

/**
 * Test for {@link NumberOfShortestPathsFinder}.
 */
public class NumberOfShortestPathsFinderTest {

    private static final String COST = "cost";

    private enum RelTypes implements RelationshipType {
        R1, R2
    }

    private NumberOfShortestPathsFinder pathFinder = new NumberOfShortestPathsFinder();
    private GraphDatabaseService database;
    private Node one;
    private Node three;

    /**
     * Graph (cost = 1 where not specified):
     * (1)-[:R1 {cost=5}]->(2)-[:R2]->(3)
     * (1)-[:R2]->(4)-[:R1 {cost=2}]->(5)-[:R1]->(3)
     * (4)<-[:R2]-(2)
     * (1)-[:R1]->(6)-[:R1 {cost=UNDEFINED})->(7)<-[:R1]-(3)
     */
    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            database.createNode(); //id=0

            one = database.createNode();
            Node two = database.createNode();
            three = database.createNode();
            Node four = database.createNode();
            Node five = database.createNode();
            Node six = database.createNode();
            Node seven = database.createNode();

            one.createRelationshipTo(two, RelTypes.R1).setProperty(COST, 5);
            two.createRelationshipTo(three, RelTypes.R2).setProperty(COST, 1);
            one.createRelationshipTo(four, RelTypes.R2).setProperty(COST, 1);
            four.createRelationshipTo(five, RelTypes.R1).setProperty(COST, 2);
            five.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
            two.createRelationshipTo(four, RelTypes.R2).setProperty(COST, 1);
            one.createRelationshipTo(six, RelTypes.R1).setProperty(COST, 1);
            six.createRelationshipTo(seven, RelTypes.R1);
            three.createRelationshipTo(seven, RelTypes.R1).setProperty(COST, 1);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void noPathsShouldBeReturnedWhenThereIsNoPath() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(5)
                .setMaxResults(10)
                .addType(DynamicRelationshipType.withName("R3"));

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, pathFinder.findPaths(input).size());
        }
    }

    @Test
    public void noPathsShouldBeFoundWhenTraversalDepthIsTooSmall() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(1)
                .setMaxResults(10);

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, pathFinder.findPaths(input).size());
        }
    }

    @Test
    public void allShortestPathsShouldBeReturned() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(3, paths.size());
            assertEquals(2, paths.get(0).length());
            assertEquals(3, paths.get(1).length());
            assertEquals(3, paths.get(2).length());
        }
    }

    @Test
    public void allShortestPathsShouldBeReturned2() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .addType(RelTypes.R1)
                .addType(RelTypes.R2);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(3, paths.size());
            assertEquals(2, paths.get(0).length());
            assertEquals(3, paths.get(1).length());
            assertEquals(3, paths.get(2).length());
        }
    }

    @Test
    public void correctShortestPathsShouldBeReturned() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .addTypeAndDirection(RelTypes.R1, OUTGOING)
                .addTypeAndDirection(RelTypes.R2, OUTGOING);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(2, paths.size());
            assertEquals(2, paths.get(0).length());
            assertEquals(3, paths.get(1).length());
        }
    }

    @Test
    public void correctShortestPathsShouldBeReturned1() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .addType(RelTypes.R1);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(1, paths.size());
            assertEquals(3, paths.get(0).length());
            assertEquals(6, Iterables.toList(paths.get(0).nodes()).get(1).getId());
        }
    }

    @Test
    public void correctShortestPathsShouldBeReturned2() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .addType(RelTypes.R2);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(1, paths.size());
            assertEquals(3, paths.get(0).length());
            assertEquals(4, Iterables.toList(paths.get(0).nodes()).get(1).getId());
        }
    }

    @Test
    public void correctShortestPathsShouldBeReturned3() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .addTypeAndDirection(RelTypes.R2, INCOMING);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(0, paths.size());
        }
    }

    @Test
    public void correctShortestPathsShouldBeReturned4() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .addTypeAndDirection(RelTypes.R2, OUTGOING);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(0, paths.size());
        }
    }

    @Test
    public void correctShortestPathsShouldBeReturned5() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .addTypeAndDirection(RelTypes.R2, BOTH);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(1, paths.size());
            assertEquals(3, paths.get(0).length());
            assertEquals(4, Iterables.toList(paths.get(0).nodes()).get(1).getId());
        }
    }

    @Test
    public void correctShortestPathsShouldBeReturned6() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .setDirection(OUTGOING);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(2, paths.size());
            assertEquals(2, paths.get(0).length());
            assertEquals(3, paths.get(1).length());
        }
    }

    @Test
    public void shouldLimitPaths() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(1);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(1, paths.size());
            assertEquals(2, paths.get(0).length());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void costPropertyMustBeSetWhenOrderingByCost() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .setSortOrder(SortOrder.LENGTH_ASC_THEN_COST_ASC);

        try (Transaction tx = database.beginTx()) {
            pathFinder.findPaths(input);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void costPropertyMustBeSetWhenOrderingByCost2() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setMaxDepth(3)
                .setMaxResults(10)
                .setSortOrder(SortOrder.LENGTH_ASC_THEN_COST_DESC);

        try (Transaction tx = database.beginTx()) {
            pathFinder.findPaths(input);
        }
    }

    @Test
    public void shouldCorrectlyOrderPaths() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setSortOrder(SortOrder.LENGTH_ASC_THEN_COST_ASC)
                .setCostProperty(COST);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(3, paths.size());

            assertEquals(2, paths.get(0).length());
            assertEquals(6, ((WeightedPath) paths.get(0)).getCost());

            assertEquals(3, paths.get(1).length());
            assertEquals(4, ((WeightedPath) paths.get(1)).getCost());

            assertEquals(3, paths.get(2).length());
            assertEquals(Long.MAX_VALUE, ((WeightedPath) paths.get(2)).getCost());
        }
    }

    @Test
    public void shouldCorrectlyOrderPaths2() {
        PathFinderInput input = new PathFinderInput(one, three)
                .setSortOrder(SortOrder.LENGTH_ASC_THEN_COST_DESC)
                .setCostProperty(COST);

        try (Transaction tx = database.beginTx()) {
            List<? extends Path> paths = pathFinder.findPaths(input);
            assertEquals(3, paths.size());

            assertEquals(2, paths.get(0).length());
            assertEquals(6, ((WeightedPath) paths.get(0)).getCost());

            assertEquals(3, paths.get(1).length());
            assertEquals(4, ((WeightedPath) paths.get(1)).getCost());

            assertEquals(3, paths.get(2).length());
            assertEquals(2, ((WeightedPath) paths.get(2)).getCost());
        }
    }
}
