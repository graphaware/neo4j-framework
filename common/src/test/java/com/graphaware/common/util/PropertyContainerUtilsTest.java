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

import com.graphaware.common.policy.inclusion.ObjectInclusionPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.common.util.PropertyContainerUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.RelationshipType.*;

/**
 * Unit test for {@link com.graphaware.common.util.PropertyContainerUtils}.
 */
public class PropertyContainerUtilsTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);

        database.execute("CREATE " +
                "(a), " +
                "(b {key:'value'})," +
                "(b)-[:test]->(a)," +
                "(c {key:'value'})");
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldConvertContainersToMap() {
        try (Transaction tx = database.beginTx()) {
            Map<Long, Node> nodeMap = propertyContainersToMap(Iterables.asList(database.getAllNodes()));
            assertEquals(0, nodeMap.get(0L).getId());
            assertEquals(1, nodeMap.get(1L).getId());
            assertEquals(2, nodeMap.get(2L).getId());
            assertEquals(3, nodeMap.size());
        }
    }

    @Test
    public void shouldFindNodeId() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(1L, id(database.getNodeById(1)));
        }
    }

    @Test
    public void shouldFindRelationshipId() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(0L, id(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING)));
        }
    }

    @Test
    public void shouldFindNodeIds() {
        try (Transaction tx = database.beginTx()) {
            assertEquals("[0, 1, 2]", Arrays.toString(ids(database.getAllNodes())));
        }
    }

    @Test
    public void shouldFindRelationshipIds() {
        try (Transaction tx = database.beginTx()) {
            assertEquals("[0]", Arrays.toString((ids(database.getAllRelationships()))));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForUnknownContainers() {
        PropertyContainer mockPropertyContainer = Mockito.mock(PropertyContainer.class);
        id(mockPropertyContainer);
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
            assertEquals(Collections.<String, Object>emptyMap(), propertiesToMap(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING)));

            assertEquals(Collections.singletonMap("key", (Object) "value"), propertiesToMap(database.getNodeById(2)));

            assertEquals(Collections.<String, Object>emptyMap(), propertiesToMap(database.getNodeById(2), new ObjectInclusionPolicy<String>() {
                @Override
                public boolean include(String object) {
                    return !"key".equals(object);
                }
            }));
        }
    }

    @Test
    public void shouldDeleteNodeWithAllRelationships() {
        database.shutdown();
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);

        database.execute("CREATE " +
                "(a), " +
                "(b {name:'node1'})," +
                "(c {name:'node2'})," +
                "(c)-[:test {key1:'value1'}]->(b)," +
                "(c)-[:test {key1:'value1'}]->(a)");

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, deleteNodeAndRelationships(database.getNodeById(2)));
            tx.success();
        }
    }

    @Test
    public void shouldSafelyGetInt() {
        populateDatabaseWithNumberProperties();
        int expected = 123;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getInt(database.getNodeById(0), "test"));
            assertEquals(expected, getInt(database.getNodeById(1), "test"));
            assertEquals(expected, getInt(database.getNodeById(2), "test"));

            try {
                getInt(database.getNodeById(3), "test");
                fail();
            } catch (ClassCastException e) {
                //ok
            }

            try {
                getInt(database.getNodeById(4), "test");
                fail();
            } catch (NotFoundException e) {
                //ok
            }

            tx.success();
        }
    }

    @Test
    public void shouldSafelyGetIntWithDefaults() {
        populateDatabaseWithNumberProperties();
        int expected = 123;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getInt(database.getNodeById(0), "test", 123));
            assertEquals(expected, getInt(database.getNodeById(1), "test", 123));
            assertEquals(expected, getInt(database.getNodeById(2), "test", 123));
            assertEquals(expected, getInt(database.getNodeById(4), "test", 123));

            try {
                getInt(database.getNodeById(3), "test", 123);
                fail();
            } catch (ClassCastException e) {
                //ok
            }

            tx.success();
        }
    }

    @Test
    public void shouldSafelyGetLong() {
        populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getLong(database.getNodeById(0), "test"));
            assertEquals(expected, getLong(database.getNodeById(1), "test"));
            assertEquals(expected, getLong(database.getNodeById(2), "test"));

            try {
                getLong(database.getNodeById(3), "test");
                fail();
            } catch (ClassCastException e) {
                //ok
            }

            try {
                getLong(database.getNodeById(4), "test");
                fail();
            } catch (NotFoundException e) {
                //ok
            }

            tx.success();
        }
    }

    @Test
    public void shouldSafelyGetLongWithDefaults() {
        populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getLong(database.getNodeById(0), "test", 123L));
            assertEquals(expected, getLong(database.getNodeById(1), "test", 123L));
            assertEquals(expected, getLong(database.getNodeById(2), "test", 123L));
            assertEquals(expected, getLong(database.getNodeById(4), "test", 123L));

            try {
                getLong(database.getNodeById(3), "test", 123L);
                fail();
            } catch (ClassCastException e) {
                //ok
            }

            tx.success();
        }
    }

    @Test
    public void shouldSafelyGetFloat() {
        populateDatabaseWithNumberProperties();
        float expected = 123.0f;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getFloat(database.getNodeById(0), "test"),0);
            assertEquals(expected, getFloat(database.getNodeById(1), "test"),0);
            assertEquals(expected, getFloat(database.getNodeById(2), "test"),0);
            assertEquals(expected, getFloat(database.getNodeById(5), "test"),0);

            try {
                getFloat(database.getNodeById(3), "test");
                fail();
            } catch (ClassCastException e) {
                //ok
            }

            try {
                getFloat(database.getNodeById(4), "test");
                fail();
            } catch (NotFoundException e) {
                //ok
            }

            tx.success();
        }
    }

    @Test
    public void shouldSafelyGetFloatWithDefaults() {
        populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getFloat(database.getNodeById(0), "test", 123L),0);
            assertEquals(expected, getFloat(database.getNodeById(1), "test", 123L),0);
            assertEquals(expected, getFloat(database.getNodeById(2), "test", 123L),0);
            assertEquals(expected, getFloat(database.getNodeById(4), "test", 123L),0);
            assertEquals(expected, getFloat(database.getNodeById(5), "test", 123L),0);

            try {
                getFloat(database.getNodeById(3), "test", 123L);
                fail();
            } catch (ClassCastException e) {
                //ok
            }

            tx.success();
        }
    }

    private void populateDatabaseWithNumberProperties() {
        database.shutdown();
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.createNode().setProperty("test", (byte) 123);
            database.createNode().setProperty("test", 123);
            database.createNode().setProperty("test", 123L);
            database.createNode().setProperty("test", "string");
            database.createNode();
            database.createNode().setProperty("test", 123.0f);
            tx.success();
        }
    }
}
