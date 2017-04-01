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

package com.graphaware.common.kv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GraphKeyValueStoreTest {

    private GraphDatabaseService database;
    private KeyValueStore store;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        store = new GraphKeyValueStore(database);
    }

    @Test
    public void shouldStoreAndRetrieveValue() {
        try (Transaction tx = database.beginTx()) {
            store.set("someString", "testString");
            store.set("someInt", 23);
            store.set("someLong", 13L);
            store.set("someShort", (short) 3);
            store.set("someByte", (byte) 3);
            store.set("someByteArray", new byte[]{1, 2, 3});
            store.set("someBool", true);
            tx.success();
        }

        try (Transaction tx = database.beginTx()) {
            assertEquals("testString", store.get("someString"));
            assertEquals("testString", store.get("someString", "bla"));
            assertEquals("testString", store.get("unknown", "testString"));

            assertEquals(23, store.get("someInt"));
            assertEquals(23, store.get("someInt", 255));
            assertEquals(23, store.get("unknown", 23));

            assertEquals(13L, store.get("someLong"));
            assertEquals(13L, store.get("someLong", 1544L));
            assertEquals(13L, store.get("unknown", 13L));

            assertEquals((short) 3, store.get("someShort"));
            assertEquals((short) 3, store.get("someShort", 1544L));
            assertEquals((short) 3, store.get("unknown", (short) 3));

            assertEquals((byte) 3, store.get("someByte"));
            assertEquals((byte) 3, store.get("someByte", 1544L));
            assertEquals((byte) 3, store.get("unknown", (byte) 3));

            assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) store.get("someByteArray"));
            assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) store.get("someByteArray", new byte[]{}));
            assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) store.get("unknown", new byte[]{1, 2, 3}));

            assertEquals(true, store.get("someBool"));
            assertEquals(true, store.get("someBool", false));
            assertEquals(true, store.get("unknown", true));

            tx.success();
        }
    }

    @After
    public void tearDown() {
        database.shutdown();
    }
}
