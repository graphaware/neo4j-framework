/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.description.predicate;

import com.graphaware.description.value.UndefinedValue;
import org.junit.Test;

import java.util.HashMap;

import static com.graphaware.description.predicate.Predicates.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link EqualTo}.
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
        assertTrue(equalTo(2).isMoreGeneralThan(equalTo(2)));
        assertTrue(equalTo(UndefinedValue.getInstance()).isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertTrue(equalTo(UndefinedValue.getInstance()).isMoreGeneralThan(undefined()));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(3)));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo(2L)));
        assertFalse(equalTo(2).isMoreGeneralThan(equalTo((short) 2)));
        assertFalse(equalTo(2).isMoreGeneralThan(greaterThan(2)));
        assertFalse(equalTo(2).isMoreGeneralThan(lessThan(2)));
        assertFalse(equalTo(2).isMoreGeneralThan(any()));
        assertTrue(equalTo(2).isMoreGeneralThan(new Or(equalTo(2), equalTo(3))));
        assertTrue(equalTo(2).isMoreGeneralThan(lessThanOrEqualTo(2)));
        assertTrue(equalTo(2).isMoreGeneralThan(greaterThanOrEqualTo(2)));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertTrue(equalTo(2).isMoreSpecificThan(equalTo(2)));
        assertTrue(equalTo(UndefinedValue.getInstance()).isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertTrue(equalTo(UndefinedValue.getInstance()).isMoreSpecificThan(undefined()));
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(3)));
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo(2L)));
        assertFalse(equalTo(2).isMoreSpecificThan(equalTo((short) 2)));
        assertFalse(equalTo(2).isMoreSpecificThan(greaterThan(2)));
        assertTrue(equalTo(2).isMoreSpecificThan(greaterThan(1)));
        assertFalse(equalTo(2).isMoreSpecificThan(lessThan(2)));
        assertTrue(equalTo(2).isMoreSpecificThan(lessThan(3)));
        assertTrue(equalTo(2).isMoreSpecificThan(any()));
        assertTrue(equalTo(2).isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertTrue(equalTo(2).isMoreSpecificThan(lessThanOrEqualTo(2)));
        assertTrue(equalTo(3).isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertTrue(equalTo(1).isMoreSpecificThan(lessThanOrEqualTo(2)));
    }

    @Test
    public void shouldCorrectlyJudgeMutuallyExclusive() {
        assertFalse(equalTo(2).isMutuallyExclusive(equalTo(2)));
        assertFalse(equalTo(UndefinedValue.getInstance()).isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertFalse(equalTo(UndefinedValue.getInstance()).isMutuallyExclusive(undefined()));
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(3)));
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo(2L)));
        assertTrue(equalTo(2).isMutuallyExclusive(equalTo((short) 2)));
        assertTrue(equalTo(2).isMutuallyExclusive(greaterThan(2)));
        assertFalse(equalTo(2).isMutuallyExclusive(greaterThan(1)));
        assertTrue(equalTo(2).isMutuallyExclusive(lessThan(2)));
        assertFalse(equalTo(2).isMutuallyExclusive(lessThan(3)));
        assertFalse(equalTo(2).isMutuallyExclusive(any()));
        assertFalse(equalTo(2).isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertFalse(equalTo(2).isMutuallyExclusive(lessThanOrEqualTo(2)));
        assertFalse(equalTo(3).isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertFalse(equalTo(1).isMutuallyExclusive(lessThanOrEqualTo(2)));
    }
}
