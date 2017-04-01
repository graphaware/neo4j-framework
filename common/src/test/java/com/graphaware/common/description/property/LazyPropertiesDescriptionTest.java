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

import com.graphaware.common.util.IterableUtils;
import org.junit.Test;

import java.util.List;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.predicate.Predicates.undefined;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.common.description.property.LazyPropertiesDescription}.
 */
public class LazyPropertiesDescriptionTest extends PropertiesDescriptionTest {    

    @Test
    public void shouldContainCorrectKeys() {
        PropertiesDescription description = lazy();

        List<String> keys = IterableUtils.toList(description.getKeys());

        assertEquals(3, keys.size());
        assertTrue(keys.contains("two"));
        assertTrue(keys.contains("three"));
        assertTrue(keys.contains("array"));
    }

    @Test
    public void shouldReturnEqualPredicatesForExistingKeys() {
        PropertiesDescription description = lazy();

        assertEquals(equalTo(2), description.get("two"));
        assertEquals(equalTo("3"), description.get("three"));
        assertEquals(equalTo(new int[]{4, 5}), description.get("array"));
    }

    @Test
    public void shouldReturnUndefinedForNonExistingKeys() {
        PropertiesDescription description = lazy();

        assertEquals(undefined(), description.get("non-existing"));
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertTrue(lazy().isMoreGeneralThan(lazy()));
        assertTrue(lazy().isMoreGeneralThan(literal()));
        assertTrue(lazy().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
        assertFalse(lazy().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
        assertFalse(lazy().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(lazy().isMoreGeneralThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(lazy().isMoreGeneralThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertTrue(lazy().isMoreSpecificThan(lazy()));
        assertTrue(lazy().isMoreSpecificThan(literal()));
        assertTrue(lazy().isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
        assertFalse(lazy().isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
        assertFalse(lazy().isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"))));
        assertTrue(lazy().isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(lazy().isMoreSpecificThan(wildcard("two", equalTo(2), "three", equalTo("4"))));
        assertFalse(lazy().isMoreSpecificThan(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
    }

    @Test
    public void shouldCorrectlyJudgeMutex() {
        assertFalse(lazy().isMutuallyExclusive(lazy()));
        assertFalse(lazy().isMutuallyExclusive(literal()));
        assertFalse(lazy().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}))));
        assertTrue(lazy().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("4"), "array", equalTo(new int[]{4, 5}))));
        assertTrue(lazy().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"))));
        assertFalse(lazy().isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("3"))));
        assertTrue(lazy().isMutuallyExclusive(wildcard("two", equalTo(2), "three", equalTo("4"))));
        assertTrue(lazy().isMutuallyExclusive(literal("two", equalTo(2), "three", equalTo("3"), "array", equalTo(new int[]{4, 5}), "four", equalTo(4))));
    }
}
