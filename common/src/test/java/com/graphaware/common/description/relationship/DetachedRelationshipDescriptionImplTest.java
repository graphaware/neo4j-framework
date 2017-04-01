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

package com.graphaware.common.description.relationship;

import com.graphaware.common.serialize.Serializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.*;

/**
 * Test for {@link com.graphaware.common.description.relationship.DetachedRelationshipDescriptionImpl}.
 */
public class DetachedRelationshipDescriptionImplTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .newGraphDatabase();

        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            Node root = database.createNode();
            Node one = database.createNode();
            root.createRelationshipTo(one, RelationshipType.withName("TEST")).setProperty("k", new int[]{2, 3, 4});
            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertTrue(literal("TEST", OUTGOING).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST2", OUTGOING).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", BOTH).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).isMoreGeneralThan(literal("TEST", BOTH)));
        assertFalse(literal("TEST", OUTGOING).isMoreGeneralThan(literal("TEST", INCOMING)));
        assertFalse(literal("TEST", INCOMING).isMoreGeneralThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).with("k1", equalTo("v1")).isMoreGeneralThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
        assertFalse(literal("TEST2", OUTGOING).with("k1", equalTo("v1")).isMoreGeneralThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertTrue(literal("TEST", OUTGOING).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST2", OUTGOING).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", BOTH).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).isMoreSpecificThan(literal("TEST", BOTH)));
        assertFalse(literal("TEST", OUTGOING).isMoreSpecificThan(literal("TEST", INCOMING)));
        assertFalse(literal("TEST", INCOMING).isMoreSpecificThan(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST", OUTGOING).with("k1", equalTo("v1")).isMoreSpecificThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
        assertFalse(literal("TEST2", OUTGOING).with("k1", equalTo("v1")).isMoreSpecificThan(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
    }

    @Test
    public void shouldCorrectlyJudgeMutex() {
        assertFalse(literal("TEST", OUTGOING).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertTrue(literal("TEST2", OUTGOING).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST", BOTH).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST", OUTGOING).isMutuallyExclusive(literal("TEST", BOTH)));
        assertTrue(literal("TEST", OUTGOING).isMutuallyExclusive(literal("TEST", INCOMING)));
        assertTrue(literal("TEST", INCOMING).isMutuallyExclusive(literal("TEST", OUTGOING)));
        assertFalse(literal("TEST", OUTGOING).with("k1", equalTo("v1")).isMutuallyExclusive(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
        assertTrue(literal("TEST2", OUTGOING).with("k1", equalTo("v1")).isMutuallyExclusive(literal("TEST", OUTGOING).with("k1", equalTo("v1"))));
    }

    @Test
    public void verifySerialization() {
        try (Transaction tx = database.beginTx()) {
            RelationshipDescription description = literal(database.getRelationshipById(0), database.getNodeById(0));

            String serialized = Serializer.toString(description, "testPrefix");
            RelationshipDescription deserialized = Serializer.fromString(serialized, "testPrefix");

            assertEquals(deserialized, description);
        }
    }

    @Test
    public void verifySerialization2() {
        try (Transaction tx = database.beginTx()) {
            RelationshipDescription description = literal(database.getRelationshipById(0), database.getNodeById(0))
                    .with("k1", equalTo("v1"))
                    .with("k2", equalTo("v2"))
                    .with("k3", equalTo("v3"))
                    .with("k4", equalTo("v4"))
                    .with("k5", equalTo("v5"))
                    .with("k6", equalTo("v6"))
                    .with("k7", equalTo(new String[]{"test1", "test2", "some very long string that should hopefully be long enough, very very very loooooooong string"}))
                    .with("k8", equalTo(new String[]{"test1", "test2", "some very long string that should hopefully be long enough, very very very loooooooong string"}))
                    .with("k10", equalTo(new String[]{"test1", "test2", "some very long string that should hopefully be long enough, very very very loooooooong string"}))
                    .with("k11", equalTo(new String[]{"test1", "test2", "some very long string that should hopefully be long enough, very very very loooooooong string"}))
                    .with("k12", equalTo(new String[]{"test1", "test2", "some very long string that should hopefully be long enough, very very very loooooooong string"}))
                    .with("k13", equalTo(new String[]{"test1", "test2", "some very long string that should hopefully be long enough, very very very loooooooong string"}));

            String serialized = Serializer.toString(description, "testPrefix");
            RelationshipDescription deserialized = Serializer.fromString(serialized, "testPrefix");

            assertEquals(deserialized, description);
        }
    }

    @Test
    public void verifySerialization3() {
        try (Transaction tx = database.beginTx()) {
            RelationshipDescription description1 = literal(database.getRelationshipById(0), database.getNodeById(0));
            String serialized1 = Serializer.toString(description1, "testPrefix");

            RelationshipDescription description2 = literal("TEST", OUTGOING).with("k", equalTo(new int[]{2, 3, 4}));
            String serialized2 = Serializer.toString(description2, "testPrefix");

            assertEquals(serialized1, serialized2);
        }
    }
}
