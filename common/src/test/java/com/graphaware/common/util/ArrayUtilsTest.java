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

import org.junit.Test;

import java.util.Collections;

import static com.graphaware.common.util.ArrayUtils.*;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.common.util.ArrayUtils}.
 */
public class ArrayUtilsTest {

    @Test
    public void shouldCorrectlyIdentifyPrimitiveArray() {
        assertTrue(isPrimitiveArray(new byte[]{}));
        assertTrue(isPrimitiveArray(new char[]{}));
        assertTrue(isPrimitiveArray(new boolean[]{}));
        assertTrue(isPrimitiveArray(new long[]{}));
        assertTrue(isPrimitiveArray(new double[]{}));
        assertTrue(isPrimitiveArray(new int[]{}));
        assertTrue(isPrimitiveArray(new short[]{}));
        assertTrue(isPrimitiveArray(new float[]{}));
        assertFalse(isPrimitiveArray(new String[]{}));
        assertFalse(isPrimitiveArray(123));
        assertFalse(isPrimitiveArray(new Object[]{}));
    }

    @Test
    public void shouldCorrectlyIdentifyPrimitiveOrStringArray() {
        assertTrue(isPrimitiveOrStringArray(new byte[]{}));
        assertTrue(isPrimitiveOrStringArray(new char[]{}));
        assertTrue(isPrimitiveOrStringArray(new boolean[]{}));
        assertTrue(isPrimitiveOrStringArray(new long[]{}));
        assertTrue(isPrimitiveOrStringArray(new double[]{}));
        assertTrue(isPrimitiveOrStringArray(new int[]{}));
        assertTrue(isPrimitiveOrStringArray(new short[]{}));
        assertTrue(isPrimitiveOrStringArray(new float[]{}));
        assertTrue(isPrimitiveOrStringArray(new String[]{}));
        assertFalse(isPrimitiveOrStringArray(123));
        assertFalse(isPrimitiveOrStringArray(new Object[]{}));
    }

    @Test
    public void shouldCorrectlyIdentifyArray() {
        assertTrue(isArray(new byte[]{}));
        assertTrue(isArray(new char[]{}));
        assertTrue(isArray(new boolean[]{}));
        assertTrue(isArray(new long[]{}));
        assertTrue(isArray(new double[]{}));
        assertTrue(isArray(new int[]{}));
        assertTrue(isArray(new short[]{}));
        assertTrue(isArray(new float[]{}));
        assertTrue(isArray(new String[]{}));
        assertTrue(isArray(new Object[]{}));
        assertFalse(isArray(123));
    }

    @Test
    public void shouldCorrectlyPrintPrimitiveArray() {
        assertEquals("[3, 4]", primitiveOrStringArrayToString(new byte[]{3, 4}));
        assertEquals("[x, y]", primitiveOrStringArrayToString(new char[]{'x', 'y'}));
        assertEquals("[true, false]", primitiveOrStringArrayToString(new boolean[]{true, false}));
        assertEquals("[3, 4]", primitiveOrStringArrayToString(new long[]{3, 4}));
        assertEquals("[3.0, 4.0]", primitiveOrStringArrayToString(new double[]{3, 4}));
        assertEquals("[3, 4]", primitiveOrStringArrayToString(new int[]{3, 4}));
        assertEquals("[3, 4]", primitiveOrStringArrayToString(new short[]{3, 4}));
        assertEquals("[3.0, 4.0]", primitiveOrStringArrayToString(new float[]{3, 4}));
        assertEquals("[3, 4]", primitiveOrStringArrayToString(new String[]{"3", "4"}));
        assertEquals("[]", primitiveOrStringArrayToString(new float[]{}));
    }

    @Test
    public void shouldCorrectlyCheckEquality() {
        assertTrue(arrayFriendlyEquals(new byte[]{1, 2}, new byte[]{1, 2}));
        assertTrue(arrayFriendlyEquals(new char[]{1, 2}, new char[]{1, 2}));
        assertTrue(arrayFriendlyEquals(new boolean[]{true, false}, new boolean[]{true, false}));
        assertTrue(arrayFriendlyEquals(new long[]{1, 2}, new long[]{1, 2}));
        assertTrue(arrayFriendlyEquals(new double[]{1, 2}, new double[]{1, 2}));
        assertTrue(arrayFriendlyEquals(new int[]{1, 2}, new int[]{1, 2}));
        assertTrue(arrayFriendlyEquals(new short[]{1, 2}, new short[]{1, 2}));
        assertTrue(arrayFriendlyEquals(new float[]{1, 2}, new float[]{1, 2}));
        assertTrue(arrayFriendlyEquals(new String[]{"1", "2"}, new String[]{"1", "2"}));
        assertTrue(arrayFriendlyEquals(new Object[]{"1", 2}, new Object[]{"1", 2}));
        assertTrue(arrayFriendlyEquals("2", "2"));
        assertTrue(arrayFriendlyEquals(2, 2));

        assertFalse(arrayFriendlyEquals(new byte[]{1, 3}, new byte[]{1, 2}));
        assertFalse(arrayFriendlyEquals(new char[]{1, 3}, new char[]{1, 2}));
        assertFalse(arrayFriendlyEquals(new boolean[]{false, false}, new boolean[]{true, false}));
        assertFalse(arrayFriendlyEquals(new long[]{1, 2}, new int[]{1, 2}));
        assertFalse(arrayFriendlyEquals(new double[]{1, 2}, new float[]{1, 2}));
        assertFalse(arrayFriendlyEquals(new int[]{1, 2}, new byte[]{1, 2}));
        assertFalse(arrayFriendlyEquals(new int[]{1, 2}, new short[]{1, 2}));
        assertFalse(arrayFriendlyEquals(new float[]{1, 2}, new Float[]{new Float(1.0), new Float(2.0)}));
        assertFalse(arrayFriendlyEquals(new Object[]{"1", "2"}, new Object[]{"1", 2}));
        assertFalse(arrayFriendlyEquals("2", "3"));
        assertFalse(arrayFriendlyEquals(2, 3));
    }

    @Test
    public void equalArraysShouldHaveEqualHasCodes() {
        assertEquals(arrayFriendlyHashCode(new byte[]{1, 2}), arrayFriendlyHashCode(new byte[]{1, 2}));
        assertEquals(arrayFriendlyHashCode(new char[]{1, 2}), arrayFriendlyHashCode(new char[]{1, 2}));
        assertEquals(arrayFriendlyHashCode(new boolean[]{true, false}), arrayFriendlyHashCode(new boolean[]{true, false}));
        assertEquals(arrayFriendlyHashCode(new long[]{1, 2}), arrayFriendlyHashCode(new long[]{1, 2}));
        assertEquals(arrayFriendlyHashCode(new double[]{1, 2}), arrayFriendlyHashCode(new double[]{1, 2}));
        assertEquals(arrayFriendlyHashCode(new int[]{1, 2}), arrayFriendlyHashCode(new int[]{1, 2}));
        assertEquals(arrayFriendlyHashCode(new short[]{1, 2}), arrayFriendlyHashCode(new short[]{1, 2}));
        assertEquals(arrayFriendlyHashCode(new float[]{1, 2}), arrayFriendlyHashCode(new float[]{1, 2}));
        assertEquals(arrayFriendlyHashCode(new String[]{"1", "2"}), arrayFriendlyHashCode(new String[]{"1", "2"}));
        assertEquals(arrayFriendlyHashCode(new Object[]{"1", 2}), arrayFriendlyHashCode(new Object[]{"1", 2}));
        assertEquals(arrayFriendlyHashCode("2"), arrayFriendlyHashCode("2"));
        assertEquals(arrayFriendlyHashCode(2), arrayFriendlyHashCode(2));

        assertNotSame(arrayFriendlyHashCode(new byte[]{1, 3}), arrayFriendlyHashCode(new byte[]{1, 2}));
        assertNotSame(arrayFriendlyHashCode(new char[]{1, 3}), arrayFriendlyHashCode(new char[]{1, 2}));
        assertNotSame(arrayFriendlyHashCode(new boolean[]{false, false}), arrayFriendlyHashCode(new boolean[]{true, false}));
        assertNotSame(arrayFriendlyHashCode(new long[]{1, 2}), arrayFriendlyHashCode(new int[]{1, 2}));
        assertNotSame(arrayFriendlyHashCode(new double[]{1, 2}), arrayFriendlyHashCode(new float[]{1, 2}));
        assertNotSame(arrayFriendlyHashCode(new int[]{1, 2}), arrayFriendlyHashCode(new byte[]{1, 2}));
        assertNotSame(arrayFriendlyHashCode(new int[]{1, 2}), arrayFriendlyHashCode(new short[]{1, 2}));
        assertNotSame(arrayFriendlyHashCode(new float[]{1, 2}), arrayFriendlyHashCode(new Float[]{new Float(1.0), new Float(2.0)}));
        assertNotSame(arrayFriendlyHashCode(new Object[]{"1", "2"}), arrayFriendlyHashCode(new Object[]{"1", 2}));
        assertNotSame(arrayFriendlyHashCode("2"), arrayFriendlyHashCode("3"));
        assertNotSame(arrayFriendlyHashCode(2), arrayFriendlyHashCode(3));
    }

    @Test
    public void equalMapsShouldBeEqual() {
        assertTrue(arrayFriendlyMapEquals(singletonMap("key", (Object) "value"), singletonMap("key", (Object) "value")));
        assertFalse(arrayFriendlyMapEquals(singletonMap("key", (Object) "value2"), singletonMap("key", (Object) "value")));

        assertTrue(arrayFriendlyMapEquals(Collections.<String, Object>singletonMap("key", new int[]{1, 2}), Collections.<String, Object>singletonMap("key", new int[]{1, 2})));
        assertFalse(arrayFriendlyMapEquals(Collections.<String, Object>singletonMap("key", new int[]{1, 3}), Collections.<String, Object>singletonMap("key", new int[]{1, 2})));
        assertFalse(arrayFriendlyMapEquals(Collections.<String, Object>singletonMap("key", new int[]{1, 3}), Collections.<String, Object>emptyMap()));
        assertFalse(arrayFriendlyMapEquals(Collections.<String, Object>singletonMap("key", new int[]{1, 3}), Collections.<String, Object>singletonMap("key", null)));
        assertFalse(arrayFriendlyMapEquals(Collections.<String, Object>singletonMap("key", null), Collections.<String, Object>singletonMap("key", new int[]{1, 3})));
        assertTrue(arrayFriendlyMapEquals(Collections.<String, Object>singletonMap("key", null), Collections.<String, Object>singletonMap("key", null)));
        assertFalse(arrayFriendlyMapEquals(Collections.<String, Object>singletonMap("key2", new int[]{1, 2}), Collections.<String, Object>singletonMap("key", new int[]{1, 2})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPrintingNonPrimitiveArray() {
        primitiveOrStringArrayToString(new Object[]{"bla"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPrintingNonArray() {
        primitiveOrStringArrayToString("bla");
    }
}
