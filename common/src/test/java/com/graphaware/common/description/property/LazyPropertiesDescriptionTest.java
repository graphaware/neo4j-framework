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

package com.graphaware.common.description.property;

import com.graphaware.common.util.IterableUtils;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Transaction;

import java.util.List;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link com.graphaware.common.description.property.LazyPropertiesDescription}.
 */
public class LazyPropertiesDescriptionTest extends PropertiesDescriptionTest {    

    @Test
    public void shouldContainCorrectKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = lazy(tx);

            List<String> keys = IterableUtils.toList(description.getKeys());

            assertEquals(3, keys.size());
            assertTrue(keys.contains("two"));
            assertTrue(keys.contains("three"));
            assertTrue(keys.contains("array"));
        }
    }

    @Test
    public void shouldReturnEqualPredicatesForExistingKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = lazy(tx);

            assertEquals(equalTo(2), description.get("two"));
            assertEquals(equalTo("3"), description.get("three"));
            assertEquals(equalTo(new int[]{4, 5}), description.get("array"));
        }
    }

    @Test
    public void shouldReturnUndefinedForNonExistingKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = lazy(tx);

            assertEquals(undefined(), description.get("non-existing"));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(lazy(tx).isMoreGeneralThan(lazy(tx)));
            assertTrue(lazy(tx).isMoreGeneralThan(literal(tx)));
            assertTrue(lazy(tx).isMoreGeneralThan(wildcard(tx)));
            assertTrue(lazy(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(lazy(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(lazy(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(lazy(tx).isMoreGeneralThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(lazy(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(lazy(tx).isMoreSpecificThan(lazy(tx)));
            assertTrue(lazy(tx).isMoreSpecificThan(literal(tx)));
            assertTrue(lazy(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(lazy(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(lazy(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"))));
            assertTrue(lazy(tx).isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(lazy(tx).isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("4"))));
            assertFalse(lazy(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMutex() {
        try (Transaction tx = database.beginTx()) {
            assertFalse(lazy(tx).isMutuallyExclusive(lazy(tx)));
            assertFalse(lazy(tx).isMutuallyExclusive(literal(tx)));
            assertFalse(lazy(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertTrue(lazy(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertTrue(lazy(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(lazy(tx).isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertTrue(lazy(tx).isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("4"))));
            assertTrue(lazy(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }
}
