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

import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Transaction;

import java.util.Collections;
import java.util.List;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static com.graphaware.common.util.IterableUtils.toList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link com.graphaware.common.description.property.LiteralPropertiesDescription}.
 */
public class LiteralPropertiesDescriptionTest extends PropertiesDescriptionTest {

    @Test
    public void shouldContainCorrectKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = literal(tx);

            List<String> keys = toList(description.getKeys());

            assertEquals(3, keys.size());
            assertTrue(keys.contains("two"));
            assertTrue(keys.contains("three"));
            assertTrue(keys.contains("array"));
        }
    }

    @Test
    public void shouldReturnEqualPredicatesForExistingKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = literal(tx);

            assertEquals(equalTo(2), description.get("two"));
            assertEquals(equalTo("3"), description.get("three"));
            assertEquals(equalTo(new int[]{4, 5}), description.get("array"));
        }
    }

    @Test
    public void shouldReturnUndefinedForNonExistingKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = literal(tx);

            assertEquals(undefined(), description.get("non-existing"));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {

        try (Transaction tx = database.beginTx()) {
            assertTrue(literal(tx).isMoreGeneralThan(literal(tx)));
            assertTrue(literal(tx).isMoreGeneralThan(lazy(tx)));
            assertTrue(literal(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(literal(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(literal(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(literal(tx).isMoreGeneralThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(literal(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(literal(tx).isMoreSpecificThan(literal(tx)));
            assertTrue(literal(tx).isMoreSpecificThan(lazy(tx)));
            assertTrue(literal(tx).isMoreSpecificThan(literal(Collections.emptyMap()).with("two", equalTo(2)).with("three", equalTo("3")).with("array", equalTo(new int[]{4, 5}))));
            assertFalse(literal(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(literal(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"))));
            assertTrue(literal(tx).isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(literal(tx).isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("4"))));
            assertFalse(literal(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMutex() {
        try (Transaction tx = database.beginTx()) {
            assertFalse(literal(tx).isMutuallyExclusive(literal(tx)));
            assertFalse(literal(tx).isMutuallyExclusive(lazy(tx)));
            assertFalse(literal(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertTrue(literal(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertTrue(literal(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(literal(tx).isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertTrue(literal(tx).isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("4"))));
            assertTrue(literal(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }
}
