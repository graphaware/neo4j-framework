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

package com.graphaware.common.description.predicate;

import org.junit.Test;

import java.util.HashMap;

import static com.graphaware.common.description.predicate.Predicates.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.common.description.predicate.EqualTo}.
 */
public class EqualToTest {

    @Test
    public void shouldEvaluateToTrueForEqualPrimitives() {
        assertTrue(equalTo((byte) 2).evaluate((byte) 2));
        assertTrue(equalTo(new Byte("3")).evaluate((byte) 3));

        assertTrue(equalTo('e').evaluate('e'));
        assertTrue(equalTo(new Character('3')).evaluate('3'));

        assertTrue(equalTo(true).evaluate(true));
        assertTrue(equalTo(Boolean.FALSE).evaluate(false));

        assertTrue(equalTo(1).evaluate(1));
        assertTrue(equalTo(new Integer("3")).evaluate(3));

        assertTrue(equalTo(123L).evaluate(123L));
        assertTrue(equalTo(1232384712957129L).evaluate(new Long("1232384712957129")));

        assertTrue(equalTo((short) 33).evaluate((short) 33));
        assertTrue(equalTo(new Short("3")).evaluate((short) 3));

        assertTrue(equalTo((float) 3.14).evaluate((float) 3.14000));
        assertTrue(equalTo(new Float(3.14)).evaluate((float) 3.14000));

        assertTrue(equalTo(3.14).evaluate(3.14000));
        assertTrue(equalTo(new Double("3.14")).evaluate(3.14000));
    }

    @Test
    public void shouldEvaluateToFalseForUnequalPrimitives() {
        assertFalse(equalTo((byte) 2).evaluate((byte) 3));
        assertFalse(equalTo(new Byte("3")).evaluate((byte) 4));
        assertFalse(equalTo((byte) 2).evaluate(2));
        assertFalse(equalTo((byte) 2).evaluate(2L));
        assertFalse(equalTo((byte) 2).evaluate((short) 2));

        assertFalse(equalTo('e').evaluate('f'));
        assertFalse(equalTo('e').evaluate("e"));
        assertFalse(equalTo(new Character('4')).evaluate('3'));
        assertFalse(equalTo('e').evaluate(3));

        assertFalse(equalTo(true).evaluate(false));
        assertFalse(equalTo(Boolean.FALSE).evaluate(true));
        assertFalse(equalTo(true).evaluate("true"));

        assertFalse(equalTo(1).evaluate(2));
        assertFalse(equalTo(1).evaluate(1L));
        assertFalse(equalTo(1).evaluate((short) 1));
        assertFalse(equalTo(1).evaluate((byte) 1));
        assertFalse(equalTo(new Integer("2")).evaluate(3));

        assertFalse(equalTo(123L).evaluate(124L));
        assertFalse(equalTo(123L).evaluate(123));
        assertFalse(equalTo(1232384712957129L).evaluate(new Long("1232384712957128")));

        assertFalse(equalTo((short) 33).evaluate((byte) 33));
        assertFalse(equalTo((short) 33).evaluate(33));
        assertFalse(equalTo((short) 33).evaluate((long) 33));
        assertFalse(equalTo((short) 33).evaluate((short) 34));
        assertFalse(equalTo(new Short("3")).evaluate((short) 4));

        assertFalse(equalTo((float) 3.14).evaluate((float) 3.14100));
        assertFalse(equalTo(new Float(3.14)).evaluate(3.14000));
        assertFalse(equalTo(new Float(3.14)).evaluate(3));

        assertFalse(equalTo(3.14).evaluate(3.14001));
        assertFalse(equalTo(new Double("3.14")).evaluate(3.14001));

        assertFalse(equalTo(1).evaluate(UndefinedValue.getInstance()));
    }

    @Test
    public void shouldEvaluateToTrueForEqualString() {
        assertTrue(equalTo("test").evaluate("test"));
        assertTrue(equalTo("").evaluate(""));
    }

    @Test
    public void shouldEvaluateToFalseForUnequalString() {
        assertFalse(equalTo("test").evaluate("test1"));
        assertFalse(equalTo("").evaluate(" "));
    }

    @Test
    public void shouldEvaluateToTrueForEqualPrimitiveArrays() {
        assertTrue(equalTo(new byte[]{2, 3, 4}).evaluate(new byte[]{2, 3, 4}));
        assertTrue(equalTo(new char[]{'2', 3, '4'}).evaluate(new char[]{'2', 3, '4'}));
        assertTrue(equalTo(new boolean[]{true, false}).evaluate(new boolean[]{true, Boolean.FALSE}));
        assertTrue(equalTo(new int[]{2, 3, 4}).evaluate(new int[]{2, 3, 4}));
        assertTrue(equalTo(new long[]{2L, 3L, 4L}).evaluate(new long[]{2L, 3L, 4L}));
        assertTrue(equalTo(new short[]{2, 3, 4}).evaluate(new short[]{2, 3, 4}));
        assertTrue(equalTo(new float[]{2.15f, 3.1988f, 4.232f}).evaluate(new float[]{2.15f, 3.1988f, 4.232f}));
        assertTrue(equalTo(new double[]{2.15, 3.1988, 4.232}).evaluate(new double[]{2.15, 3.1988, 4.232}));
    }

    @Test
    public void shouldEvaluateToFalseForUnequalPrimitiveArrays() {
        assertFalse(equalTo(new byte[]{2, 3, 4}).evaluate(new byte[]{2, 3, 5}));
        assertFalse(equalTo(new byte[]{2, 3, 4}).evaluate(new byte[]{2, 3}));
        assertFalse(equalTo(new byte[]{2, 3, 4}).evaluate(2));
        assertFalse(equalTo(new char[]{'2', 3, '4'}).evaluate(new char[]{'2', 3, 4}));
        assertFalse(equalTo(new boolean[]{true, false}).evaluate(new boolean[]{false, Boolean.FALSE}));
        assertFalse(equalTo(new int[]{2, 3, 4}).evaluate(new int[]{2, 3, 4, 6}));
        assertFalse(equalTo(new long[]{2L, 3L, 4L}).evaluate(new long[]{2L, 3L, 5}));
        assertFalse(equalTo(new short[]{2, 3, 4}).evaluate(new short[]{2, 3, 5}));
        assertFalse(equalTo(new float[]{2.15f, 3.1988f, 4.232f}).evaluate(new float[]{2.151f, 3.1988f, 4.232f}));
        assertFalse(equalTo(new double[]{2.15, 3.1988, 4.232}).evaluate(new double[]{2.151, 3.1988, 4.232}));
    }

    @Test
    public void shouldEvaluateToTrueForEqualStringArrays() {
        assertTrue(equalTo(new String[]{"test", "test2"}).evaluate(new String[]{"test", "test2"}));
        //this will actually fail in Neo:
        assertTrue(equalTo(new String[]{"test", null}).evaluate(new String[]{"test", null}));
    }

    @Test
    public void shouldEvaluateToFalseForUnequalStringArrays() {
        assertFalse(equalTo(new String[]{"test2", "test1"}).evaluate(new String[]{"test", "test2"}));
        assertFalse(equalTo(new String[]{"test", "test2"}).evaluate(new String[]{"test", null}));
    }

    @Test
    public void shouldComplainWhenProvidedWithIllegalValues() {
        try {
            equalTo(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            equalTo(new Integer[]{});
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            equalTo(new HashMap<String, String>());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            equalTo(2).evaluate(new Byte[]{});
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            equalTo(2).evaluate(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            equalTo(2).evaluate(new HashMap<>());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void undefinedValuesShouldBeEqual() {
        assertTrue(equalTo(UndefinedValue.getInstance()).evaluate(UndefinedValue.getInstance()));
    }


    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(equalTo(2).isMoreGeneralThan(undefined()));
        assertFalse(equalTo(2).isMoreGeneralThan(any()));
        assertFalse(equalTo("abc").isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(equalTo("abc").isMoreGeneralThan(undefined()));
        assertFalse(equalTo("abc").isMoreGeneralThan(any()));

        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(1)));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(1L)));
        assertTrue(equalTo(2).isMoreGeneralThan(equalTo(2)));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(2L)));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(3)));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(3L)));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(new int[]{3})));
        assertFalse(equalTo("abc").isMoreGeneralThan(equalTo("abb")));
        assertTrue(equalTo("abc").isMoreGeneralThan(equalTo("abc")));
        assertFalse(equalTo("abc").isMoreGeneralThan(equalTo("abd")));

        assertFalse(equalTo(2).isMoreGeneralThan(greaterThan(1)));
        assertFalse(equalTo(2).isMoreGeneralThan(greaterThan(1L)));
        assertFalse(equalTo(2).isMoreGeneralThan(greaterThan(2)));
        assertFalse(equalTo(2).isMoreGeneralThan(greaterThan(3)));
        assertFalse(equalTo("abc").isMoreGeneralThan(greaterThan("abb")));
        assertFalse(equalTo("abc").isMoreGeneralThan(greaterThan("abc")));
        assertFalse(equalTo("abc").isMoreGeneralThan(greaterThan("abd")));

        assertFalse(equalTo(2).isMoreGeneralThan(lessThan(1)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThan(1L)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThan(2)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThan(3)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThan(3L)));
        assertFalse(equalTo("abc").isMoreGeneralThan(lessThan("abb")));
        assertFalse(equalTo("abc").isMoreGeneralThan(lessThan("abc")));
        assertFalse(equalTo("abc").isMoreGeneralThan(lessThan("abd")));

        assertFalse(equalTo(2).isMoreGeneralThan(greaterThanOrEqualTo(1)));
        assertFalse(equalTo(2).isMoreGeneralThan(greaterThanOrEqualTo(2)));
        assertFalse(equalTo(2).isMoreGeneralThan(greaterThanOrEqualTo(3)));
        assertFalse(equalTo(2).isMoreGeneralThan(greaterThanOrEqualTo(3L)));
        assertFalse(equalTo("abc").isMoreGeneralThan(greaterThanOrEqualTo("abb")));
        assertFalse(equalTo("abc").isMoreGeneralThan(greaterThanOrEqualTo("abc")));
        assertFalse(equalTo("abc").isMoreGeneralThan(greaterThanOrEqualTo("abd")));

        assertFalse(equalTo(2).isMoreGeneralThan(lessThanOrEqualTo(1)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThanOrEqualTo(1L)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThanOrEqualTo(2)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThanOrEqualTo(3)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThanOrEqualTo(3L)));
        assertFalse(equalTo("abc").isMoreGeneralThan(lessThanOrEqualTo("abb")));
        assertFalse(equalTo("abc").isMoreGeneralThan(lessThanOrEqualTo("abc")));
        assertFalse(equalTo("abc").isMoreGeneralThan(lessThanOrEqualTo("abd")));

        assertFalse(equalTo(2).isMoreGeneralThan(new Or(greaterThan(1), equalTo(1))));
        assertFalse(equalTo(2).isMoreGeneralThan(new Or(greaterThan(2), equalTo(2))));
        assertFalse(equalTo(2).isMoreGeneralThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(equalTo(2).isMoreGeneralThan(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(equalTo("abc").isMoreGeneralThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertFalse(equalTo("abc").isMoreGeneralThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(equalTo("abc").isMoreGeneralThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(equalTo(2).isMoreGeneralThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(equalTo(2).isMoreGeneralThan(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(equalTo(2).isMoreGeneralThan(new Or(lessThan(2), equalTo(2))));
        assertFalse(equalTo(2).isMoreGeneralThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(equalTo(2).isMoreGeneralThan(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(equalTo("abc").isMoreGeneralThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(equalTo("abc").isMoreGeneralThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(equalTo("abc").isMoreGeneralThan(new Or(lessThan("abd"), equalTo("abd"))));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(equalTo(2).isMoreSpecificThan(undefined()));
        assertTrue(equalTo(2).isMoreSpecificThan(any()));
        assertFalse(equalTo("abc").isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(equalTo("abc").isMoreSpecificThan(undefined()));
        assertTrue(equalTo("abc").isMoreSpecificThan(any()));

        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(1)));
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(1L)));
        assertTrue(equalTo(2).isMoreSpecificThan(equalTo(2)));
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(3)));
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(3L)));
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(new int[]{3})));
        assertFalse(equalTo("abc").isMoreSpecificThan(equalTo("abb")));
        assertTrue(equalTo("abc").isMoreSpecificThan(equalTo("abc")));
        assertFalse(equalTo("abc").isMoreSpecificThan(equalTo("abd")));

        assertTrue(equalTo(2).isMoreSpecificThan(greaterThan(1)));
        assertFalse(equalTo(2).isMoreSpecificThan(greaterThan(1L)));
        assertFalse(equalTo(2).isMoreSpecificThan(greaterThan(2)));
        assertFalse(equalTo(2).isMoreSpecificThan(greaterThan(3)));
        assertTrue(equalTo("abc").isMoreSpecificThan(greaterThan("abb")));
        assertFalse(equalTo("abc").isMoreSpecificThan(greaterThan("abc")));
        assertFalse(equalTo("abc").isMoreSpecificThan(greaterThan("abd")));

        assertFalse(equalTo(2).isMoreSpecificThan(lessThan(1)));
        assertFalse(equalTo(2).isMoreSpecificThan(lessThan(1L)));
        assertFalse(equalTo(2).isMoreSpecificThan(lessThan(2)));
        assertTrue(equalTo(2).isMoreSpecificThan(lessThan(3)));
        assertFalse(equalTo(2).isMoreSpecificThan(lessThan(3L)));
        assertFalse(equalTo("abc").isMoreSpecificThan(lessThan("abb")));
        assertFalse(equalTo("abc").isMoreSpecificThan(lessThan("abc")));
        assertTrue(equalTo("abc").isMoreSpecificThan(lessThan("abd")));

        assertTrue(equalTo(2).isMoreSpecificThan(greaterThanOrEqualTo(1)));
        assertTrue(equalTo(2).isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(equalTo(2).isMoreSpecificThan(greaterThanOrEqualTo(3)));
        assertFalse(equalTo(2).isMoreSpecificThan(greaterThanOrEqualTo(3L)));
        assertTrue(equalTo("abc").isMoreSpecificThan(greaterThanOrEqualTo("abb")));
        assertTrue(equalTo("abc").isMoreSpecificThan(greaterThanOrEqualTo("abc")));
        assertFalse(equalTo("abc").isMoreSpecificThan(greaterThanOrEqualTo("abd")));

        assertFalse(equalTo(2).isMoreSpecificThan(lessThanOrEqualTo(1)));
        assertFalse(equalTo(2).isMoreSpecificThan(lessThanOrEqualTo(1L)));
        assertTrue(equalTo(2).isMoreSpecificThan(lessThanOrEqualTo(2)));
        assertTrue(equalTo(2).isMoreSpecificThan(lessThanOrEqualTo(3)));
        assertFalse(equalTo(2).isMoreSpecificThan(lessThanOrEqualTo(3L)));
        assertFalse(equalTo("abc").isMoreSpecificThan(lessThanOrEqualTo("abb")));
        assertTrue(equalTo("abc").isMoreSpecificThan(lessThanOrEqualTo("abc")));
        assertTrue(equalTo("abc").isMoreSpecificThan(lessThanOrEqualTo("abd")));

        assertTrue(equalTo(2).isMoreSpecificThan(new Or(greaterThan(1), equalTo(1))));
        assertTrue(equalTo(2).isMoreSpecificThan(new Or(greaterThan(2), equalTo(2))));
        assertFalse(equalTo(2).isMoreSpecificThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(equalTo(2).isMoreSpecificThan(new Or(greaterThan(3L), equalTo(3L))));
        assertTrue(equalTo("abc").isMoreSpecificThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertTrue(equalTo("abc").isMoreSpecificThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(equalTo("abc").isMoreSpecificThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(equalTo(2).isMoreSpecificThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(equalTo(2).isMoreSpecificThan(new Or(lessThan(1L), equalTo(1L))));
        assertTrue(equalTo(2).isMoreSpecificThan(new Or(lessThan(2), equalTo(2))));
        assertTrue(equalTo(2).isMoreSpecificThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(equalTo(2).isMoreSpecificThan(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(equalTo("abc").isMoreSpecificThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertTrue(equalTo("abc").isMoreSpecificThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertTrue(equalTo("abc").isMoreSpecificThan(new Or(lessThan("abd"), equalTo("abd"))));
    }

    @Test
    public void shouldCorrectlyJudgeMutuallyExclusive() {
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(equalTo(2).isMutuallyExclusive(undefined()));
        assertFalse(equalTo(2).isMutuallyExclusive(any()));
        assertTrue(equalTo("abc").isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(equalTo("abc").isMutuallyExclusive(undefined()));
        assertFalse(equalTo("abc").isMutuallyExclusive(any()));

        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(1)));
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(1L)));
        assertFalse(equalTo(2).isMutuallyExclusive(equalTo(2)));
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(3)));
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(3L)));
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(new int[]{3})));
        assertTrue(equalTo("abc").isMutuallyExclusive(equalTo("abb")));
        assertFalse(equalTo("abc").isMutuallyExclusive(equalTo("abc")));
        assertTrue(equalTo("abc").isMutuallyExclusive(equalTo("abd")));

        assertFalse(equalTo(2).isMutuallyExclusive(greaterThan(1)));
        assertTrue(equalTo(2).isMutuallyExclusive(greaterThan(1L)));
        assertTrue(equalTo(2).isMutuallyExclusive(greaterThan(2)));
        assertTrue(equalTo(2).isMutuallyExclusive(greaterThan(3)));
        assertFalse(equalTo("abc").isMutuallyExclusive(greaterThan("abb")));
        assertTrue(equalTo("abc").isMutuallyExclusive(greaterThan("abc")));
        assertTrue(equalTo("abc").isMutuallyExclusive(greaterThan("abd")));

        assertTrue(equalTo(2).isMutuallyExclusive(lessThan(1)));
        assertTrue(equalTo(2).isMutuallyExclusive(lessThan(1L)));
        assertTrue(equalTo(2).isMutuallyExclusive(lessThan(2)));
        assertFalse(equalTo(2).isMutuallyExclusive(lessThan(3)));
        assertTrue(equalTo(2).isMutuallyExclusive(lessThan(3L)));
        assertTrue(equalTo("abc").isMutuallyExclusive(lessThan("abb")));
        assertTrue(equalTo("abc").isMutuallyExclusive(lessThan("abc")));
        assertFalse(equalTo("abc").isMutuallyExclusive(lessThan("abd")));

        assertFalse(equalTo(2).isMutuallyExclusive(greaterThanOrEqualTo(1)));
        assertFalse(equalTo(2).isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertTrue(equalTo(2).isMutuallyExclusive(greaterThanOrEqualTo(3)));
        assertTrue(equalTo(2).isMutuallyExclusive(greaterThanOrEqualTo(3L)));
        assertFalse(equalTo("abc").isMutuallyExclusive(greaterThanOrEqualTo("abb")));
        assertFalse(equalTo("abc").isMutuallyExclusive(greaterThanOrEqualTo("abc")));
        assertTrue(equalTo("abc").isMutuallyExclusive(greaterThanOrEqualTo("abd")));

        assertTrue(equalTo(2).isMutuallyExclusive(lessThanOrEqualTo(1)));
        assertTrue(equalTo(2).isMutuallyExclusive(lessThanOrEqualTo(1L)));
        assertFalse(equalTo(2).isMutuallyExclusive(lessThanOrEqualTo(2)));
        assertFalse(equalTo(2).isMutuallyExclusive(lessThanOrEqualTo(3)));
        assertTrue(equalTo(2).isMutuallyExclusive(lessThanOrEqualTo(3L)));
        assertTrue(equalTo("abc").isMutuallyExclusive(lessThanOrEqualTo("abb")));
        assertFalse(equalTo("abc").isMutuallyExclusive(lessThanOrEqualTo("abc")));
        assertFalse(equalTo("abc").isMutuallyExclusive(lessThanOrEqualTo("abd")));

        assertFalse(equalTo(2).isMutuallyExclusive(new Or(greaterThan(1), equalTo(1))));
        assertFalse(equalTo(2).isMutuallyExclusive(new Or(greaterThan(2), equalTo(2))));
        assertTrue(equalTo(2).isMutuallyExclusive(new Or(greaterThan(3), equalTo(3))));
        assertTrue(equalTo(2).isMutuallyExclusive(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(equalTo("abc").isMutuallyExclusive(new Or(greaterThan("abb"), equalTo("abb"))));
        assertFalse(equalTo("abc").isMutuallyExclusive(new Or(greaterThan("abc"), equalTo("abc"))));
        assertTrue(equalTo("abc").isMutuallyExclusive(new Or(greaterThan("abd"), equalTo("abd"))));

        assertTrue(equalTo(2).isMutuallyExclusive(new Or(lessThan(1), equalTo(1))));
        assertTrue(equalTo(2).isMutuallyExclusive(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(equalTo(2).isMutuallyExclusive(new Or(lessThan(2), equalTo(2))));
        assertFalse(equalTo(2).isMutuallyExclusive(new Or(lessThan(3), equalTo(3))));
        assertTrue(equalTo(2).isMutuallyExclusive(new Or(lessThan(3L), equalTo(3L))));
        assertTrue(equalTo("abc").isMutuallyExclusive(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(equalTo("abc").isMutuallyExclusive(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(equalTo("abc").isMutuallyExclusive(new Or(lessThan("abd"), equalTo("abd"))));
    }

    @Test
    public void equalPredicatesShouldBeEqualAndHaveSameHashCode() {
        assertEquals(equalTo(3), equalTo(3));
        assertEquals(equalTo(3).hashCode(), equalTo(3).hashCode());

        assertEquals(equalTo(new byte[]{1,2,3}), equalTo(new byte[]{1,2,3}));
        assertEquals(equalTo(new byte[]{1,2,3}).hashCode(), equalTo(new byte[]{1,2,3}).hashCode());
    }
}
