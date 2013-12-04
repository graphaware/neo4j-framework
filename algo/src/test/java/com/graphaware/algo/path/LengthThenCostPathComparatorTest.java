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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphalgo.impl.path.AllPaths;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.kernel.Traversal;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.PathExpanders.*;
import static org.neo4j.helpers.collection.Iterables.*;

/**
 * Unit test for {@link LengthThenCostPathComparator}.
 */
public class LengthThenCostPathComparatorTest {

    private static final RelationshipType TEST = DynamicRelationshipType.withName("TEST");
    private static final String COST = "cost";

    private GraphDatabaseService database;
    private Node one;
    private Node three;

    /**
     * Graph (default cost = 1):
     * (1)-(cost=5)->(2)-->(3)
     * (1)-->(4)-(cost=2)->(5)-->(3)
     * (4)-->(2)
     * (1)-->(6)-(cost=INFINITY)->(7)-->(3)
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

            one.createRelationshipTo(two, TEST).setProperty(COST, 5);
            two.createRelationshipTo(three, TEST).setProperty(COST, 1);
            one.createRelationshipTo(four, TEST).setProperty(COST, 1);
            four.createRelationshipTo(five, TEST).setProperty(COST, 2);
            five.createRelationshipTo(three, TEST).setProperty(COST, 1);
            four.createRelationshipTo(two, TEST).setProperty(COST, 1);
            one.createRelationshipTo(six, TEST).setProperty(COST, 1);
            six.createRelationshipTo(seven, TEST);
            seven.createRelationshipTo(three, TEST).setProperty(COST, 1);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCorrectlyOrderPaths() {
        try (Transaction tx = database.beginTx()) {
            List<Path> paths = toList(new AllPaths(10, forDirection(OUTGOING)).findAllPaths(one, three));
            Collections.sort(paths, new LengthThenCostPathComparator(COST));

            assertEquals(4, paths.size());
            assertEquals("(1)--[TEST,0]-->(2)--[TEST,1]-->(3)", paths.get(0).toString());
            assertEquals("(1)--[TEST,2]-->(4)--[TEST,5]-->(2)--[TEST,1]-->(3)", paths.get(1).toString());
            assertEquals("(1)--[TEST,2]-->(4)--[TEST,3]-->(5)--[TEST,4]-->(3)", paths.get(2).toString());
            assertEquals("(1)--[TEST,6]-->(6)--[TEST,7]-->(7)--[TEST,8]-->(3)", paths.get(3).toString());
        }
    }
}
