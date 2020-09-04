/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.util;

import com.graphaware.common.junit.InjectNeo4j;
import com.graphaware.common.junit.Neo4jExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.util.DirectionUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.RelationshipType.withName;

/**
 * Unit test for {@link com.graphaware.common.util.DirectionUtils}.
 */
@TestInstance(PER_CLASS)
@ExtendWith(Neo4jExtension.class)
public class DirectionUtilsTest {

    @InjectNeo4j(lifecycle = InjectNeo4j.Lifecycle.CLASS)
    private GraphDatabaseService database;

    @BeforeAll
    protected void populate() {
        try (Transaction tx = database.beginTx()) {
            Node node1 = tx.createNode();
            Node node2 = tx.createNode();
            node1.createRelationshipTo(node2, withName("test"));

            Node node3 = tx.createNode();
            node3.createRelationshipTo(node3, withName("test"));

            tx.commit();
        }
    }

    @Test
    public void outgoingShouldBeCorrectlyIdentified() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(OUTGOING, resolveDirection(tx.getNodeById(0).getSingleRelationship(withName("test"), OUTGOING), tx.getNodeById(0)));
        }
    }

    @Test
    public void incomingShouldBeCorrectlyIdentified() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(INCOMING, resolveDirection(tx.getNodeById(0).getSingleRelationship(withName("test"), OUTGOING), tx.getNodeById(1)));
        }
    }

    @Test
    public void relationshipToSelfShouldBeBoth() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(BOTH, resolveDirection(tx.getNodeById(2).getSingleRelationship(withName("test"), OUTGOING), tx.getNodeById(2)));
            assertEquals(BOTH, resolveDirection(tx.getNodeById(2).getSingleRelationship(withName("test"), INCOMING), tx.getNodeById(2)));
        }
    }

    @Test
    public void relationshipToSelfShouldHonourDefault() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(OUTGOING, resolveDirection(tx.getNodeById(2).getSingleRelationship(withName("test"), OUTGOING), tx.getNodeById(2), OUTGOING));
            assertEquals(OUTGOING, resolveDirection(tx.getNodeById(2).getSingleRelationship(withName("test"), INCOMING), tx.getNodeById(2), OUTGOING));
            assertEquals(INCOMING, resolveDirection(tx.getNodeById(2).getSingleRelationship(withName("test"), OUTGOING), tx.getNodeById(2), INCOMING));
            assertEquals(INCOMING, resolveDirection(tx.getNodeById(2).getSingleRelationship(withName("test"), INCOMING), tx.getNodeById(2), INCOMING));
        }
    }

    @Test
    public void invalidResolutionShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (Transaction tx = database.beginTx()) {
                resolveDirection((tx.getNodeById(0).getSingleRelationship(withName("test"), OUTGOING)), tx.getNodeById(2));
            }
        });
    }

    @Test
    public void verifyMatching() {
        try (Transaction tx = database.beginTx()) {
            Node nodeThree = tx.getNodeById(2);
            Relationship selfRelationship = nodeThree.getSingleRelationship(withName("test"), OUTGOING);

            assertTrue(matches(selfRelationship, nodeThree, OUTGOING));
            assertTrue(matches(selfRelationship, nodeThree, INCOMING));
            assertTrue(matches(selfRelationship, nodeThree, BOTH));

            Node nodeOne = tx.getNodeById(0);
            Node nodeTwo = tx.getNodeById(1);
            Relationship relationship = nodeOne.getSingleRelationship(withName("test"), OUTGOING);

            assertTrue(matches(relationship, nodeOne, OUTGOING));
            assertTrue(matches(relationship, nodeOne, BOTH));
            assertFalse(matches(relationship, nodeOne, INCOMING));

            assertTrue(matches(relationship, nodeTwo, INCOMING));
            assertTrue(matches(relationship, nodeTwo, BOTH));
            assertFalse(matches(relationship, nodeTwo, OUTGOING));
        }
    }

    @Test
    public void verifyMatching2() {
        assertTrue(matches(BOTH, OUTGOING));
        assertTrue(matches(BOTH, INCOMING));
        assertTrue(matches(INCOMING, BOTH));
        assertTrue(matches(OUTGOING, BOTH));
        assertTrue(matches(BOTH, BOTH));
        assertTrue(matches(INCOMING, INCOMING));
        assertTrue(matches(OUTGOING, OUTGOING));
        assertFalse(matches(INCOMING, OUTGOING));
        assertFalse(matches(OUTGOING, INCOMING));
    }

    @Test
    public void verifyIncorrectMatching() {
        assertThrows(IllegalArgumentException.class, () -> {
            try (Transaction tx = database.beginTx()) {
                Relationship relationship = tx.getNodeById(0).getSingleRelationship(withName("test"), OUTGOING);

                matches(relationship, tx.getNodeById(2), OUTGOING);
            }
        });
    }

    @Test
    public void shouldCorrectlyReverseDirection() {
        assertEquals(OUTGOING, reverse(INCOMING));
        assertEquals(INCOMING, reverse(OUTGOING));
        assertEquals(BOTH, reverse(BOTH));
    }
}
