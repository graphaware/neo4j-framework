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

import static com.graphaware.common.description.predicate.Predicates.any;
import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link com.graphaware.common.description.property.WildcardPropertiesDescription}.
 */
public class WildcardPropertiesDescriptionTest extends PropertiesDescriptionTest {

    @Test
    public void shouldContainCorrectKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = wildcard(tx);

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
            PropertiesDescription description = wildcard(tx);

            assertEquals(equalTo(2), description.get("two"));
            assertEquals(equalTo("3"), description.get("three"));
            assertEquals(equalTo(new int[]{4, 5}), description.get("array"));
        }
    }

    @Test
    public void shouldReturnWildcardForNonExistingKeys() {
        try (Transaction tx = database.beginTx()) {
            PropertiesDescription description = wildcard(tx);

            assertEquals(any(), description.get("non-existing"));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(wildcard(tx).isMoreGeneralThan(wildcard(tx)));
            assertTrue(wildcard(tx).isMoreGeneralThan(literal(tx)));
            assertTrue(wildcard(tx).isMoreGeneralThan(lazy(tx)));
            assertTrue(wildcard(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(wildcard(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertTrue(wildcard(tx).with("three", any()).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(wildcard(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(wildcard(tx).isMoreGeneralThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertTrue(wildcard(tx).isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        try (Transaction tx = database.beginTx()) {
            assertTrue(wildcard(tx).isMoreSpecificThan(wildcard(tx)));
            assertTrue(wildcard(tx).isMoreSpecificThan(literal(tx)));
            assertTrue(wildcard(tx).isMoreSpecificThan(lazy(tx)));
            assertTrue(wildcard(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(wildcard(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertFalse(wildcard(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"))));
            assertTrue(wildcard(tx).isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(wildcard(tx).with("three", any()).isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(wildcard(tx).isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("4"))));
            assertFalse(wildcard(tx).isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }

    @Test
    public void shouldCorrectlyJudgeMutex() {
        try (Transaction tx = database.beginTx()) {
            assertFalse(wildcard(tx).isMutuallyExclusive(wildcard(tx)));
            assertFalse(wildcard(tx).isMutuallyExclusive(literal(tx)));
            assertFalse(wildcard(tx).isMutuallyExclusive(lazy(tx)));
            assertFalse(wildcard(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
            assertTrue(wildcard(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
            assertTrue(wildcard(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(wildcard(tx).with("array", any()).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(wildcard(tx).isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertFalse(wildcard(tx).isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("3"))));
            assertTrue(wildcard(tx).isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("4"))));
            assertFalse(wildcard(tx).isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
        }
    }
}
