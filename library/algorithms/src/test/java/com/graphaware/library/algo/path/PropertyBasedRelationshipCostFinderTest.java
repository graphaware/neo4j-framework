/*
 * Copyright (c) 2014 GraphAware
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

package com.graphaware.library.algo.path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.library.algo.path.PropertyBasedRelationshipCostFinder} implementations.
 */
public class PropertyBasedRelationshipCostFinderTest {

    private static final RelationshipType TEST = DynamicRelationshipType.withName("TEST");
    private static final String COST = "cost";

    private GraphDatabaseService database;
    private Relationship r;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (Transaction tx = database.beginTx()) {
            Node one = database.createNode();
            Node two = database.createNode();

            r = one.createRelationshipTo(two, TEST);
            r.setProperty(COST, 5);

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldReadCostFromProperty() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(5, new ZeroDefaultingRelationshipCostFinder(COST).getCost(r));
            assertEquals(5, new MaxLongDefaultingRelationshipCostFinder(COST).getCost(r));
        }
    }

    @Test
    public void shouldReadCostFromProperty2() {
        try (Transaction tx = database.beginTx()) {
            r.setProperty(COST, 5L);
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(5, new ZeroDefaultingRelationshipCostFinder(COST).getCost(r));
            assertEquals(5, new MaxLongDefaultingRelationshipCostFinder(COST).getCost(r));
        }
    }

    @Test
    public void shouldReturnDefaultCostWhenPropertyDoesNotExist() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new ZeroDefaultingRelationshipCostFinder("non-existing").getCost(r));
            assertEquals(Long.MAX_VALUE, new MaxLongDefaultingRelationshipCostFinder("non-existing").getCost(r));
        }
    }

    @Test
    public void shouldReturnDefaultCostWhenPropertyNotNumerical() {
        try (Transaction tx = database.beginTx()) {
            r.setProperty(COST, "NaN");
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals(0, new ZeroDefaultingRelationshipCostFinder(COST).getCost(r));
            assertEquals(Long.MAX_VALUE, new MaxLongDefaultingRelationshipCostFinder(COST).getCost(r));
        }
    }
}
