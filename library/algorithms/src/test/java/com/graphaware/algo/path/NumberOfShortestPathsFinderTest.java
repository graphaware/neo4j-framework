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

package com.graphaware.algo.path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.PathExpanders.*;
import static org.neo4j.kernel.Traversal.pathExpanderForAllTypes;
import static org.neo4j.kernel.Traversal.pathExpanderForTypes;

/**
 * Unit test for {@link NumberOfShortestPathsFinder}.
 */
public class NumberOfShortestPathsFinderTest {

    private enum RelTypes implements RelationshipType {
        R1, R2
    }

    private GraphDatabaseService database;
    private Node one;
    private Node three;

    /**
     * Graph:
     * (1)-[:R2]->(2)-[:R1]->(3)
     * (1)-[:R1]->(4)-[:R1]->(5)-[:R1]->(3)
     * (4)-[:R1]->(2)
     */
    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            one = database.createNode();
            Node two = database.createNode();
            three = database.createNode();
            Node four = database.createNode();
            Node five = database.createNode();

            one.createRelationshipTo(two, RelTypes.R2);
            two.createRelationshipTo(three, RelTypes.R1);
            one.createRelationshipTo(four, RelTypes.R1);
            four.createRelationshipTo(five, RelTypes.R1);
            five.createRelationshipTo(three, RelTypes.R1);
            four.createRelationshipTo(two, RelTypes.R1);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void noPathsShouldBeReturnedWhenThereIsNoPath() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new NumberOfShortestPathsFinder(5, 10, forTypeAndDirection(RelTypes.R2, OUTGOING)).findPaths(one, three).size());
        }
    }

    @Test
    public void noPathsShouldBeFoundWhenTraversalDepthIsTooSmall() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new NumberOfShortestPathsFinder(2, 10, forTypeAndDirection(RelTypes.R1, OUTGOING)).findPaths(one, three).size());
        }
    }

    @Test
    public void allShortestPathsShouldBeReturned() {
        try (Transaction tx = database.beginTx()) {
            List<Path> paths = new NumberOfShortestPathsFinder(3, 10, forTypeAndDirection(RelTypes.R1, OUTGOING)).findPaths(one, three);
            assertEquals(2, paths.size());
            assertEquals(3, paths.get(0).length());
            assertEquals(3, paths.get(1).length());

            paths = new NumberOfShortestPathsFinder(3, 10, forDirection(OUTGOING)).findPaths(one, three);
            assertEquals(2, paths.size());
            assertEquals(2, paths.get(0).length());
            assertEquals(3, paths.get(1).length());
        }
    }

    @Test
    public void shouldLimitPaths() {
        try (Transaction tx = database.beginTx()) {
            List<Path> paths = new NumberOfShortestPathsFinder(3, 1, forDirection(OUTGOING)).findPaths(one, three);
            assertEquals(1, paths.size());
            assertEquals(2, paths.get(0).length());
        }
    }
}
