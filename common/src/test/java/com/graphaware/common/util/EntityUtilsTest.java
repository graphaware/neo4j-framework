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
import com.graphaware.common.policy.inclusion.ObjectInclusionPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.graphdb.*;
import org.neo4j.internal.helpers.collection.Iterables;

import java.util.*;

import static com.graphaware.common.util.EntityUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.RelationshipType.withName;

/**
 * Unit test for {@link EntityUtils}.
 */
@ExtendWith(Neo4jExtension.class)
public class EntityUtilsTest {

    @InjectNeo4j
    private GraphDatabaseService database;

    private long a, b, c, r;

    @Test
    public void shouldConvertEntitiesToMap() {
        populate();

        try (Transaction tx = database.beginTx()) {
            Map<Long, Node> nodeMap = entitiesToMap(Iterables.asList(tx.getAllNodes()));
            assertEquals(a, nodeMap.get(a).getId());
            assertEquals(b, nodeMap.get(b).getId());
            assertEquals(c, nodeMap.get(c).getId());
            assertEquals(3, nodeMap.size());
        }
    }

    @Test
    public void shouldFindIds() {
        populate();

        try (Transaction tx = database.beginTx()) {
            assertEquals("[" + r + "]", Arrays.toString((ids(tx.getAllRelationships()))));
        }
        try (Transaction tx = database.beginTx()) {
            assertEquals("[" + a + ", " + b + ", " + c + "]", Arrays.toString(ids(tx.getAllNodes())));
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
        populate();

        try (Transaction tx = database.beginTx()) {
            assertEquals(Collections.<String, Object>emptyMap(), propertiesToMap(tx.getNodeById(b).getSingleRelationship(withName("test"), OUTGOING)));

            assertEquals(Collections.singletonMap("key", (Object) "value"), propertiesToMap(tx.getNodeById(c)));

            assertEquals(Collections.<String, Object>emptyMap(), propertiesToMap(tx.getNodeById(c), new ObjectInclusionPolicy<String>() {
                @Override
                public boolean include(String object) {
                    return !"key".equals(object);
                }
            }));
        }
    }

    @Test
    public void shouldDeleteNodeWithAllRelationships() {
        database.executeTransactionally("CREATE " +
                "(a), " +
                "(b {name:'node1'})," +
                "(c:ToDelete {name:'node2'})," +
                "(c)-[:test {key1:'value1'}]->(b)," +
                "(c)-[:test {key1:'value1'}]->(a)");

        try (Transaction tx = database.beginTx()) {
            assertEquals(2, deleteNodeAndRelationships(tx.findNodes(Label.label("ToDelete")).next()));
            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetInt() {
        Long[] ids = populateDatabaseWithNumberProperties();
        int expected = 123;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getInt(tx.getNodeById(ids[0]), "test"));
            assertEquals(expected, getInt(tx.getNodeById(ids[1]), "test"));
            assertEquals(expected, getInt(tx.getNodeById(ids[2]), "test"));

            assertThrows(ClassCastException.class, () -> {
                getInt(tx.getNodeById(ids[3]), "test");
            });

            assertThrows(NotFoundException.class, () -> {
                getInt(tx.getNodeById(ids[4]), "test");
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetIntWithDefaults() {
        Long[] ids = populateDatabaseWithNumberProperties();
        int expected = 123;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getInt(tx.getNodeById(ids[0]), "test", 123));
            assertEquals(expected, getInt(tx.getNodeById(ids[1]), "test", 123));
            assertEquals(expected, getInt(tx.getNodeById(ids[2]), "test", 123));
            assertEquals(expected, getInt(tx.getNodeById(ids[4]), "test", 123));

            assertThrows(ClassCastException.class, () -> {
                getInt(tx.getNodeById(ids[3]), "test", 123);
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetLong() {
        Long[] ids = populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getLong(tx.getNodeById(ids[0]), "test"));
            assertEquals(expected, getLong(tx.getNodeById(ids[1]), "test"));
            assertEquals(expected, getLong(tx.getNodeById(ids[2]), "test"));

            assertThrows(ClassCastException.class, () -> {
                getLong(tx.getNodeById(ids[3]), "test");
            });

            assertThrows(NotFoundException.class, () -> {
                getLong(tx.getNodeById(ids[4]), "test");
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetLongWithDefaults() {
        Long[] ids = populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getLong(tx.getNodeById(ids[0]), "test", 123L));
            assertEquals(expected, getLong(tx.getNodeById(ids[1]), "test", 123L));
            assertEquals(expected, getLong(tx.getNodeById(ids[2]), "test", 123L));
            assertEquals(expected, getLong(tx.getNodeById(ids[4]), "test", 123L));

            assertThrows(ClassCastException.class, () -> {
                getLong(tx.getNodeById(ids[3]), "test", 123L);
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetFloat() {
        Long[] ids = populateDatabaseWithNumberProperties();
        float expected = 123.0f;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getFloat(tx.getNodeById(ids[0]), "test"), 0);
            assertEquals(expected, getFloat(tx.getNodeById(ids[1]), "test"), 0);
            assertEquals(expected, getFloat(tx.getNodeById(ids[2]), "test"), 0);
            assertEquals(expected, getFloat(tx.getNodeById(ids[5]), "test"), 0);

            assertThrows(ClassCastException.class, () -> {
                getFloat(tx.getNodeById(ids[3]), "test");
            });

            assertThrows(NotFoundException.class, () -> {
                getFloat(tx.getNodeById(ids[4]), "test");
            });

            tx.commit();
        }
    }

    @Test
    public void shouldSafelyGetFloatWithDefaults() {
        Long[] ids = populateDatabaseWithNumberProperties();
        long expected = 123L;

        try (Transaction tx = database.beginTx()) {
            assertEquals(expected, getFloat(tx.getNodeById(ids[0]), "test", 123L), 0);
            assertEquals(expected, getFloat(tx.getNodeById(ids[1]), "test", 123L), 0);
            assertEquals(expected, getFloat(tx.getNodeById(ids[2]), "test", 123L), 0);
            assertEquals(expected, getFloat(tx.getNodeById(ids[4]), "test", 123L), 0);
            assertEquals(expected, getFloat(tx.getNodeById(ids[5]), "test", 123L), 0);

            assertThrows(ClassCastException.class, () -> {
                getFloat(tx.getNodeById(ids[3]), "test", 123L);
            });

            tx.commit();
        }
    }

    private Long[] populateDatabaseWithNumberProperties() {
        List<Long> result = new LinkedList<>();

        try (Transaction tx = database.beginTx()) {
            Node node = tx.createNode();
            node.setProperty("test", (byte) 123);
            result.add(node.getId());

            node = tx.createNode();
            node.setProperty("test", 123);
            result.add(node.getId());

            node = tx.createNode();
            node.setProperty("test", 123L);
            result.add(node.getId());

            node = tx.createNode();
            node.setProperty("test", "string");
            result.add(node.getId());

            result.add(tx.createNode().getId());

            node = tx.createNode();
            node.setProperty("test", 123.0f);
            result.add(node.getId());

            tx.commit();
        }

        return result.toArray(new Long[0]);
    }

    private void populate() {
        Map<String, Object> result = database.executeTransactionally("CREATE " +
                        "(a), " +
                        "(b {key:'value'})," +
                        "(b)-[r:test]->(a)," +
                        "(c {key:'value'}) RETURN id(a) as a, id(b) as b, id(c) as c, id (r) as r", Collections.emptyMap(),
                Result::next);

        a = (long) result.get("a");
        b = (long) result.get("b");
        c = (long) result.get("c");
        r = (long) result.get("r");
    }
}
