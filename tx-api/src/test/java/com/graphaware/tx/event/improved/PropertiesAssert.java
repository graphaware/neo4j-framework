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

package com.graphaware.tx.event.improved;

import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.MapUtil;

import java.util.Map;

import static com.graphaware.common.util.ArrayUtils.arrayFriendlyEquals;
import static com.graphaware.common.util.ArrayUtils.arrayFriendlyMapEquals;
import static com.graphaware.tx.event.improved.LazyTransactionDataComprehensiveTest.*;
import static org.junit.Assert.*;

public final class PropertiesAssert {

    public static void assertProperties(PropertyContainer actual, Object... keyValueKeyValue) {
        Map<String, Object> expected = MapUtil.map(keyValueKeyValue);

        assertEquals(expected.size(), Iterables.count(actual.getPropertyKeys()));
        assertEquals(expected.size(), actual.getAllProperties().size());
        assertTrue(arrayFriendlyMapEquals(expected, actual.getAllProperties()));

        for (String key : expected.keySet()) {
            assertTrue(arrayFriendlyEquals(expected.get(key), actual.getProperty(key)));
            assertTrue(arrayFriendlyEquals(expected.get(key), actual.getProperty(key, "Some Irrelevant Default")));
            assertTrue(arrayFriendlyEquals(expected.get(key), actual.getProperties(key).get(key)));
            assertTrue(arrayFriendlyEquals(expected.get(key), actual.getAllProperties().get(key)));
            assertTrue(actual.hasProperty(key));
        }

        for (String key : new String[]{NAME, COUNT, TAG, TAGS, TIME, PLACE}) {
            if (!expected.containsKey(key)) {
                assertFalse(actual.hasProperty(key));
            }
        }

        assertEquals("nothing", actual.getProperty("definitely-does-not-exist", "nothing"));

        try {
            actual.getProperty("definitely-does-not-exist");
            fail();
        } catch (NotFoundException e) {
            //ok
        }

        assertEquals(0, actual.getProperties("definitely-does-not-exist").size());
    }

    private PropertiesAssert() {
    }
}
