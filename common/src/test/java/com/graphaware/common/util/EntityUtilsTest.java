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

import com.graphaware.common.UnitTest;
import com.graphaware.common.policy.inclusion.ObjectInclusionPolicy;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.internal.helpers.collection.Iterables;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.graphaware.common.util.EntityUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.RelationshipType.withName;

/**
 * Unit test for {@link EntityUtils}.
 */
public class EntityUtilsTest extends UnitTest {

    @Override
    protected void populate(Transaction database) {
        database.execute("CREATE " +
                "(a), " +
                "(b {key:'value'})," +
                "(b)-[:test]->(a)," +
                "(c {key:'value'})");
    }

    @Test
    public void shouldConvertEntitiesToMap() {
        try (Transaction tx = database.beginTx()) {
            Map<Long, Node> nodeMap = entitiesToMap(Iterables.asList(tx.getAllNodes()));
            assertEquals(0, nodeMap.get(0L).getId());
            assertEquals(1, nodeMap.get(1L).getId());
            assertEquals(2, nodeMap.get(2L).getId());
            assertEquals(3, nodeMap.size());
        }
    }

    @Test
    public void shouldFindNodeIds() {
        try (Transaction tx = database.beginTx()) {
            assertEquals("[0, 1, 2]", Arrays.toString(ids(tx.getAllNodes())));
        }
    }

    @Test
    public void shouldFindRelationshipIds() {
        try (Transaction tx = database.beginTx()) {
            assertEquals("[0]", Arrays.toString((ids(tx.getAllRelationships()))));
        }
    }

    @Test
    public void verifyValueToString() {
        assertEquals("", valueToString(null));
        assertEquals("", valueToString(""));
        assertEquals("T", valueToString("T"));
        assertEquals("1", valueToString(1));
        assertEquals("1", valueToString(1L));
        assertEquals("[one, two]", valueToString(new String[]{"one", "two"}));
        assertEquals("[1, 2]", valueToString(new int[]{1, 2}));
    }

    @Test
    public void verifyPropertiesToMap() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(Collections.<String, Object>emptyMap(), propertiesToMap(tx.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING)));

            assertEquals(Collections.singletonMap("key", (Object) "value"), propertiesToMap(tx.getNodeById(2)));

            assertEquals(Collections.<String, Object>emptyMap(), propertiesToMap(tx.getNodeById(2), new ObjectInclusionPolicy<String>() {
                @Override
                public boolean include(String object) {
                    return !"key".equals(object);
                }
            }));
        }
    }

    @Test
    public void shouldDeleteNodeWithAllRelationships() {
        destroyDatabase();
        createDatabase();

        database.executeTransactionally("CREATE " +
                "(a), " +
                "(b {name:'node1'})," +
                "(c {name:'node2'})," +
                "(c)-[:test {key1:'value1'}]->(b)," +
                "(c)-[:test {key1:'value1'}]->(a)");

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, deleteNodeAndRelationships(tx.getNodeById(2)));
            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetInt() {
        populateDatabaseWithNumberProperties();
        int expected = 123;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getInt(tx.getNodeById(0), "test"));
            assertEquals(expected, getInt(tx.getNodeById(1), "test"));
            assertEquals(expected, getInt(tx.getNodeById(2), "test"));

            assertThrows(ClassCastException.class, () -> {
                getInt(tx.getNodeById(3), "test");
            });

            assertThrows(NotFoundException.class, () -> {
                getInt(tx.getNodeById(4), "test");
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetIntWithDefaults() {
        populateDatabaseWithNumberProperties();
        int expected = 123;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getInt(tx.getNodeById(0), "test", 123));
            assertEquals(expected, getInt(tx.getNodeById(1), "test", 123));
            assertEquals(expected, getInt(tx.getNodeById(2), "test", 123));
            assertEquals(expected, getInt(tx.getNodeById(4), "test", 123));

            assertThrows(ClassCastException.class, () -> {
                getInt(tx.getNodeById(3), "test", 123);
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetLong() {
        populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getLong(tx.getNodeById(0), "test"));
            assertEquals(expected, getLong(tx.getNodeById(1), "test"));
            assertEquals(expected, getLong(tx.getNodeById(2), "test"));

            assertThrows(ClassCastException.class, () -> {
                getLong(tx.getNodeById(3), "test");
            });

            assertThrows(NotFoundException.class, () -> {
                getLong(tx.getNodeById(4), "test");
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetLongWithDefaults() {
        populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getLong(tx.getNodeById(0), "test", 123L));
            assertEquals(expected, getLong(tx.getNodeById(1), "test", 123L));
            assertEquals(expected, getLong(tx.getNodeById(2), "test", 123L));
            assertEquals(expected, getLong(tx.getNodeById(4), "test", 123L));

            assertThrows(ClassCastException.class, () -> {
                getLong(tx.getNodeById(3), "test", 123L);
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetFloat() {
        populateDatabaseWithNumberProperties();
        float expected = 123.0f;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getFloat(tx.getNodeById(0), "test"),0);
            assertEquals(expected, getFloat(tx.getNodeById(1), "test"),0);
            assertEquals(expected, getFloat(tx.getNodeById(2), "test"),0);
            assertEquals(expected, getFloat(tx.getNodeById(5), "test"),0);

            assertThrows(ClassCastException.class, () -> {
                getFloat(tx.getNodeById(3), "test");
            });

            assertThrows(NotFoundException.class, () -> {
                getFloat(tx.getNodeById(4), "test");
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetFloatWithDefaults() {
        populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getFloat(tx.getNodeById(0), "test", 123L),0);
            assertEquals(expected, getFloat(tx.getNodeById(1), "test", 123L),0);
            assertEquals(expected, getFloat(tx.getNodeById(2), "test", 123L),0);
            assertEquals(expected, getFloat(tx.getNodeById(4), "test", 123L),0);
            assertEquals(expected, getFloat(tx.getNodeById(5), "test", 123L),0);

            assertThrows(ClassCastException.class, () -> {
                getFloat(tx.getNodeById(3), "test", 123L);
            });

            tx.commit();
        }
    }

    private void populateDatabaseWithNumberProperties() {
        destroyDatabase();
        createDatabase();

        try (Transaction tx = database.beginTx()) {
            tx.createNode().setProperty("test", (byte) 123);
            tx.createNode().setProperty("test", 123);
            tx.createNode().setProperty("test", 123L);
            tx.createNode().setProperty("test", "string");
            tx.createNode();
            tx.createNode().setProperty("test", 123.0f);
            tx.commit();
        }
    }
}
