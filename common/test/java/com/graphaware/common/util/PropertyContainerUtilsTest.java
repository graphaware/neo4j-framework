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

package com.graphaware.common.util;

import com.graphaware.common.change.Change;
import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.test.TestDataBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.common.util.PropertyContainerUtils.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;
import static org.neo4j.helpers.collection.MapUtil.stringMap;

/**
 * Unit test for {@link com.graphaware.common.util.PropertyContainerUtils}.
 */
public class PropertyContainerUtilsTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        new TestDataBuilder(database)
                .node().setProp("", "e").setProp("key", "value")
                .relationshipTo(0, "test")
                .node().setProp("key", "value");
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void shouldConvertContainersToMap() {
        Map<Long, Node> nodeMap = propertyContainersToMap(Iterables.toList(database.getAllNodes()));
        assertEquals(0, nodeMap.get(0L).getId());
        assertEquals(1, nodeMap.get(1L).getId());
        assertEquals(2, nodeMap.get(2L).getId());
        assertEquals(3, nodeMap.size());
    }

    @Test
    public void shouldConvertChangesToMap() {
        Change<Node> nodeChange = new Change<>(database.getNodeById(0), database.getNodeById(0));
        Map<Long, Change<Node>> changeMap = changesToMap(asList(nodeChange));
        assertEquals(0, changeMap.get(0L).getCurrent().getId());
        assertEquals(0, changeMap.get(0L).getPrevious().getId());
        assertEquals(1, changeMap.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenConvertingInvalidChanges() {
        Change<Node> nodeChange = new Change<>(database.getNodeById(0), database.getNodeById(1));
        changesToMap(asList(nodeChange));
    }

    @Test
    public void shouldFindNodeId() {
        assertEquals(1L, id(database.getNodeById(1)));
    }

    @Test
    public void shouldFindRelationshipId() {
        assertEquals(0L, id(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING)));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForUnknownContainers() {
        PropertyContainer mockPropertyContainer = Mockito.mock(PropertyContainer.class);
        id(mockPropertyContainer);
    }

    @Test
    public void verifyKeyCleaning() {
        assertEquals("R", cleanKey("R"));

        try {
            cleanKey(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            cleanKey("");
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            cleanKey(" ");
            fail();
        } catch (IllegalArgumentException e) {
            //OK
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
    public void verifyStringPropertyCleaning() {
        assertEquals(Collections.<String, String>emptyMap(), cleanStringProperties(Collections.<String, String>emptyMap()));
        assertEquals(stringMap("key", "value"), cleanStringProperties(stringMap("key", "value")));
        assertEquals(stringMap("key", ""), cleanStringProperties(stringMap("key", "")));
        assertEquals(stringMap("key", ""), cleanStringProperties(stringMap("key", null)));

        try {
            cleanStringProperties(stringMap(" ", "value"));
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void verifyObjectPropertyCleaning() {
        assertEquals(Collections.<String, Object>emptyMap(), cleanObjectProperties(Collections.<String, Object>emptyMap()));
        assertEquals(Collections.singletonMap("key", (Object) "value"), cleanObjectProperties(Collections.singletonMap("key", (Object) "value")));
        assertEquals(Collections.singletonMap("key", null), cleanObjectProperties(Collections.singletonMap("key", null)));
        assertEquals(Collections.singletonMap("key", (Object) 3L), cleanObjectProperties(Collections.singletonMap("key", (Object) 3L)));

        try {
            cleanStringProperties(stringMap(" ", "value"));
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void verifyPropertiesToStringMap() {
        assertEquals(Collections.<String, String>emptyMap(), propertiesToStringMap(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING)));

        try {
            propertiesToStringMap(database.getNodeById(1));
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        assertEquals(stringMap("key", "value"), propertiesToStringMap(database.getNodeById(2)));

        assertEquals(Collections.<String, String>emptyMap(), propertiesToStringMap(database.getNodeById(2), new InclusionStrategy<String>() {
            @Override
            public boolean include(String object) {
                return !"key".equals(object);
            }
        }));
    }

    @Test
    public void verifyPropertiesToObjectMap() {
        assertEquals(Collections.<String, Object>emptyMap(), propertiesToObjectMap(database.getNodeById(1).getSingleRelationship(withName("test"), OUTGOING)));

        try {
            propertiesToObjectMap(database.getNodeById(1));
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        assertEquals(Collections.singletonMap("key", (Object) "value"), propertiesToObjectMap(database.getNodeById(2)));

        assertEquals(Collections.<String, Object>emptyMap(), propertiesToObjectMap(database.getNodeById(2), new InclusionStrategy<String>() {
            @Override
            public boolean include(String object) {
                return !"key".equals(object);
            }
        }));
    }

    @Test
    public void shouldDeleteNodeWithAllRelationships() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        new TestDataBuilder(database)
                .node().setProp("name", "node1")
                .node().setProp("name", "node2")
                .relationshipTo(1, "test").setProp("key1", "value1")
                .relationshipTo(0, "test").setProp("key1", "value1");

        Transaction tx = database.beginTx();
        try {
            assertEquals(2, deleteNodeAndRelationships(database.getNodeById(2)));
        } finally {
            tx.finish();
        }
    }
}