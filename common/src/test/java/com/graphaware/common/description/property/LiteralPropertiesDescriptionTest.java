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

package com.graphaware.common.description.property;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static com.graphaware.common.util.IterableUtils.toList;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.common.description.property.LiteralPropertiesDescription}.
 */
public class LiteralPropertiesDescriptionTest extends com.graphaware.common.description.property.PropertiesDescriptionTest {

    @Test
    public void shouldContainCorrectKeys() {
        PropertiesDescription description = literal();

        List<String> keys = toList(description.getKeys());

        assertEquals(3, keys.size());
        assertTrue(keys.contains("two"));
        assertTrue(keys.contains("three"));
        assertTrue(keys.contains("array"));
    }

    @Test
    public void shouldReturnEqualPredicatesForExistingKeys() {
        PropertiesDescription description = literal();

        assertEquals(equalTo(2), description.get("two"));
        assertEquals(equalTo("3"), description.get("three"));
        assertEquals(equalTo(new int[]{4, 5}), description.get("array"));
    }

    @Test
    public void shouldReturnUndefinedForNonExistingKeys() {
        PropertiesDescription description = literal();

        assertEquals(undefined(), description.get("non-existing"));
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertTrue(literal().isMoreGeneralThan(literal()));
        assertTrue(literal().isMoreGeneralThan(lazy()));
        assertTrue(literal().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
        assertFalse(literal().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
        assertFalse(literal().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(literal().isMoreGeneralThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(literal().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertTrue(literal().isMoreSpecificThan(literal()));
        assertTrue(literal().isMoreSpecificThan(lazy()));
        assertTrue(literal().isMoreSpecificThan(literal(Collections.emptyMap()).with("two", equalTo(2)).with("three", equalTo("3")).with("array", equalTo(new int[]{4, 5}))));
        assertFalse(literal().isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
        assertFalse(literal().isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"))));
        assertTrue(literal().isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(literal().isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("4"))));
        assertFalse(literal().isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
    }

    @Test
    public void shouldCorrectlyJudgeMutex() {
        assertFalse(literal().isMutuallyExclusive(literal()));
        assertFalse(literal().isMutuallyExclusive(lazy()));
        assertFalse(literal().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
        assertTrue(literal().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
        assertTrue(literal().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(literal().isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("3"))));
        assertTrue(literal().isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("4"))));
        assertTrue(literal().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
    }
}
