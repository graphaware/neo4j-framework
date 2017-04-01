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
 * Unit test for {@link com.graphaware.common.description.predicate.LessThan}.
 */
public class LessThanTest {

    @Test
    public void shouldEvaluateToTrueForSmallerPrimitives() {
        assertTrue(lessThan((byte) 2).evaluate((byte) 0));
        assertTrue(lessThan((byte) 2).evaluate((byte) 1));
        assertTrue(lessThan(new Byte("3")).evaluate((byte) 2));

        assertTrue(lessThan('e').evaluate('c'));
        assertTrue(lessThan('e').evaluate('d'));
        assertTrue(lessThan(new Character('3')).evaluate('2'));

        assertTrue(lessThan(true).evaluate(false));
        assertTrue(lessThan(Boolean.TRUE).evaluate(false));

        assertTrue(lessThan(1).evaluate(-1));
        assertTrue(lessThan(1).evaluate(0));
        assertTrue(lessThan(new Integer("3")).evaluate(2));

        assertTrue(lessThan(123L).evaluate(121L));
        assertTrue(lessThan(123L).evaluate(122L));
        assertTrue(lessThan(1232384712957129L).evaluate(new Long("1232384712957128")));

        assertTrue(lessThan((short) 33).evaluate((short) 31));
        assertTrue(lessThan((short) 33).evaluate((short) 32));
        assertTrue(lessThan(new Short("3")).evaluate((short) 2));

        assertTrue(lessThan((float) 3.14).evaluate((float) 3.13999));
        assertTrue(lessThan(new Float(3.14)).evaluate((float) 3.13999));

        assertTrue(lessThan(3.14).evaluate(3.13999));
        assertTrue(lessThan(new Double("3.14")).evaluate(3.13999));
    }

    @Test
    public void shouldEvaluateToFalseForNotSmallerPrimitivesAndPrimitivesOfOtherTypes() {
        assertFalse(lessThan((byte) 2).evaluate((byte) 2));
        assertFalse(lessThan((byte) 2).evaluate((byte) 3));
        assertFalse(lessThan(new Byte("3")).evaluate((byte) 3));
        assertFalse(lessThan((byte) 2).evaluate(1));
        assertFalse(lessThan((byte) 2).evaluate(1L));
        assertFalse(lessThan((byte) 2).evaluate((short) 1));
        assertFalse(lessThan((byte) 2).evaluate(new byte[]{0, 1}));

        assertFalse(lessThan('e').evaluate('e'));
        assertFalse(lessThan('e').evaluate("d"));

        assertFalse(lessThan(false).evaluate(true));
        assertFalse(lessThan(Boolean.TRUE).evaluate(true));
        assertFalse(lessThan(false).evaluate("false"));

        assertFalse(lessThan(1).evaluate(1));
        assertFalse(lessThan(1).evaluate(0L));
        assertFalse(lessThan(1).evaluate((short) 0));
        assertFalse(lessThan(1).evaluate((byte) 0));
        assertFalse(lessThan(new Integer("2")).evaluate(2));

        assertFalse(lessThan(123L).evaluate(123L));
        assertFalse(lessThan(123L).evaluate(122));
        assertFalse(lessThan(1232384712957129L).evaluate(new Long("1232384712957129")));

        assertFalse(lessThan((short) 33).evaluate((byte) 32));
        assertFalse(lessThan((short) 33).evaluate(32));
        assertFalse(lessThan((short) 33).evaluate((long) 32));
        assertFalse(lessThan((short) 33).evaluate((short) 33));
        assertFalse(lessThan(new Short("3")).evaluate((short) 3));

        assertFalse(lessThan((float) 3.14).evaluate((float) 3.14001));
        assertFalse(lessThan(new Float(3.14)).evaluate(3.1401));
        assertFalse(lessThan(new Float(3.14)).evaluate(4));

        assertFalse(lessThan(3.14).evaluate(3.140001));
        assertFalse(lessThan(new Double("3.14")).evaluate(3.140001));

        assertFalse(lessThan(1).evaluate(UndefinedValue.getInstance()));
    }

    @Test
    public void shouldEvaluateToTrueForSmallerString() {
        assertTrue(lessThan("abc").evaluate("abb"));
        assertTrue(lessThan("q").evaluate(""));
    }

    @Test
    public void shouldEvaluateToFalseForNotSmallerString() {
        assertFalse(lessThan("abc").evaluate("abc"));
        assertFalse(lessThan("abc").evaluate("abd"));
        assertFalse(lessThan("").evaluate(""));
    }

    @Test
    public void shouldEvaluateToFalseForUndefined() {
        assertFalse(lessThan("abc").evaluate(UndefinedValue.getInstance()));
        assertFalse(lessThan(2).evaluate(UndefinedValue.getInstance()));
    }

    @Test
    public void shouldComplainWhenProvidedWithIllegalValues() {
        try {
            lessThan(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            lessThan(2).evaluate(new Byte[]{});
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            lessThan(2).evaluate(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            lessThan(2).evaluate(new HashMap<>());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertFalse(lessThan(2).isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(lessThan(2).isMoreGeneralThan(undefined()));
        assertFalse(lessThan(2).isMoreGeneralThan(any()));
        assertFalse(lessThan("abc").isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(lessThan("abc").isMoreGeneralThan(undefined()));
        assertFalse(lessThan("abc").isMoreGeneralThan(any()));

        assertTrue(lessThan(2).isMoreGeneralThan(equalTo(1)));
        assertFalse(lessThan(2).isMoreGeneralThan(equalTo(1L)));
        assertFalse(lessThan(2).isMoreGeneralThan(equalTo(2)));
        assertFalse(lessThan(2).isMoreGeneralThan(equalTo(3)));
        assertFalse(lessThan(2).isMoreGeneralThan(equalTo(3L)));
        assertFalse(lessThan(2).isMoreGeneralThan(equalTo(new int[]{3})));
        assertTrue(lessThan("abc").isMoreGeneralThan(equalTo("abb")));
        assertFalse(lessThan("abc").isMoreGeneralThan(equalTo("abc")));
        assertFalse(lessThan("abc").isMoreGeneralThan(equalTo("abd")));

        assertFalse(lessThan(2).isMoreGeneralThan(greaterThan(1)));
        assertFalse(lessThan(2).isMoreGeneralThan(greaterThan(1L)));
        assertFalse(lessThan(2).isMoreGeneralThan(greaterThan(2)));
        assertFalse(lessThan(2).isMoreGeneralThan(greaterThan(3)));
        assertFalse(lessThan("abc").isMoreGeneralThan(greaterThan("abb")));
        assertFalse(lessThan("abc").isMoreGeneralThan(greaterThan("abc")));
        assertFalse(lessThan("abc").isMoreGeneralThan(greaterThan("abd")));

        assertTrue(lessThan(2).isMoreGeneralThan(lessThan(1)));
        assertFalse(lessThan(2).isMoreGeneralThan(lessThan(1L)));
        assertTrue(lessThan(2).isMoreGeneralThan(lessThan(2)));
        assertFalse(lessThan(2).isMoreGeneralThan(lessThan(3)));
        assertFalse(lessThan(2).isMoreGeneralThan(lessThan(3L)));
        assertTrue(lessThan("abc").isMoreGeneralThan(lessThan("abb")));
        assertTrue(lessThan("abc").isMoreGeneralThan(lessThan("abc")));
        assertFalse(lessThan("abc").isMoreGeneralThan(lessThan("abd")));

        assertFalse(lessThan(2).isMoreGeneralThan(greaterThanOrEqualTo(1)));
        assertFalse(lessThan(2).isMoreGeneralThan(greaterThanOrEqualTo(2)));
        assertFalse(lessThan(2).isMoreGeneralThan(greaterThanOrEqualTo(3)));
        assertFalse(lessThan(2).isMoreGeneralThan(greaterThanOrEqualTo(3L)));
        assertFalse(lessThan("abc").isMoreGeneralThan(greaterThanOrEqualTo("abb")));
        assertFalse(lessThan("abc").isMoreGeneralThan(greaterThanOrEqualTo("abc")));
        assertFalse(lessThan("abc").isMoreGeneralThan(greaterThanOrEqualTo("abd")));

        assertTrue(lessThan(2).isMoreGeneralThan(lessThanOrEqualTo(1)));
        assertFalse(lessThan(2).isMoreGeneralThan(lessThanOrEqualTo(1L)));
        assertFalse(lessThan(2).isMoreGeneralThan(lessThanOrEqualTo(2)));
        assertFalse(lessThan(2).isMoreGeneralThan(lessThanOrEqualTo(3)));
        assertFalse(lessThan(2).isMoreGeneralThan(lessThanOrEqualTo(3L)));
        assertTrue(lessThan("abc").isMoreGeneralThan(lessThanOrEqualTo("abb")));
        assertFalse(lessThan("abc").isMoreGeneralThan(lessThanOrEqualTo("abc")));
        assertFalse(lessThan("abc").isMoreGeneralThan(lessThanOrEqualTo("abd")));

        assertFalse(lessThan(2).isMoreGeneralThan(new Or(greaterThan(1), equalTo(1))));
        assertFalse(lessThan(2).isMoreGeneralThan(new Or(greaterThan(2), equalTo(2))));
        assertFalse(lessThan(2).isMoreGeneralThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(lessThan(2).isMoreGeneralThan(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(lessThan("abc").isMoreGeneralThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertFalse(lessThan("abc").isMoreGeneralThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(lessThan("abc").isMoreGeneralThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertTrue(lessThan(2).isMoreGeneralThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(lessThan(2).isMoreGeneralThan(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(lessThan(2).isMoreGeneralThan(new Or(lessThan(2), equalTo(2))));
        assertFalse(lessThan(2).isMoreGeneralThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(lessThan(2).isMoreGeneralThan(new Or(lessThan(3L), equalTo(3L))));
        assertTrue(lessThan("abc").isMoreGeneralThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(lessThan("abc").isMoreGeneralThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(lessThan("abc").isMoreGeneralThan(new Or(lessThan("abd"), equalTo("abd"))));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertFalse(lessThan(2).isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(lessThan(2).isMoreSpecificThan(undefined()));
        assertTrue(lessThan(2).isMoreSpecificThan(any()));
        assertFalse(lessThan("abc").isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(lessThan("abc").isMoreSpecificThan(undefined()));
        assertTrue(lessThan("abc").isMoreSpecificThan(any()));

        assertFalse(lessThan(2).isMoreSpecificThan(equalTo(1)));
        assertFalse(lessThan(2).isMoreSpecificThan(equalTo(1L)));
        assertFalse(lessThan(2).isMoreSpecificThan(equalTo(2)));
        assertFalse(lessThan(2).isMoreSpecificThan(equalTo(3)));
        assertFalse(lessThan(2).isMoreSpecificThan(equalTo(3L)));
        assertFalse(lessThan(2).isMoreSpecificThan(equalTo(new int[]{3})));
        assertFalse(lessThan("abc").isMoreSpecificThan(equalTo("abb")));
        assertFalse(lessThan("abc").isMoreSpecificThan(equalTo("abc")));
        assertFalse(lessThan("abc").isMoreSpecificThan(equalTo("abd")));

        assertFalse(lessThan(2).isMoreSpecificThan(greaterThan(1)));
        assertFalse(lessThan(2).isMoreSpecificThan(greaterThan(1L)));
        assertFalse(lessThan(2).isMoreSpecificThan(greaterThan(2)));
        assertFalse(lessThan(2).isMoreSpecificThan(greaterThan(3)));
        assertFalse(lessThan("abc").isMoreSpecificThan(greaterThan("abb")));
        assertFalse(lessThan("abc").isMoreSpecificThan(greaterThan("abc")));
        assertFalse(lessThan("abc").isMoreSpecificThan(greaterThan("abd")));

        assertFalse(lessThan(2).isMoreSpecificThan(lessThan(1)));
        assertFalse(lessThan(2).isMoreSpecificThan(lessThan(1L)));
        assertTrue(lessThan(2).isMoreSpecificThan(lessThan(2)));
        assertTrue(lessThan(2).isMoreSpecificThan(lessThan(3)));
        assertFalse(lessThan(2).isMoreSpecificThan(lessThan(3L)));
        assertFalse(lessThan("abc").isMoreSpecificThan(lessThan("abb")));
        assertTrue(lessThan("abc").isMoreSpecificThan(lessThan("abc")));
        assertTrue(lessThan("abc").isMoreSpecificThan(lessThan("abd")));

        assertFalse(lessThan(2).isMoreSpecificThan(greaterThanOrEqualTo(1)));
        assertFalse(lessThan(2).isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(lessThan(2).isMoreSpecificThan(greaterThanOrEqualTo(3)));
        assertFalse(lessThan(2).isMoreSpecificThan(greaterThanOrEqualTo(3L)));
        assertFalse(lessThan("abc").isMoreSpecificThan(greaterThanOrEqualTo("abb")));
        assertFalse(lessThan("abc").isMoreSpecificThan(greaterThanOrEqualTo("abc")));
        assertFalse(lessThan("abc").isMoreSpecificThan(greaterThanOrEqualTo("abd")));

        assertFalse(lessThan(2).isMoreSpecificThan(lessThanOrEqualTo(1)));
        assertFalse(lessThan(2).isMoreSpecificThan(lessThanOrEqualTo(1L)));
        assertTrue(lessThan(2).isMoreSpecificThan(lessThanOrEqualTo(2)));
        assertTrue(lessThan(2).isMoreSpecificThan(lessThanOrEqualTo(3)));
        assertFalse(lessThan(2).isMoreSpecificThan(lessThanOrEqualTo(3L)));
        assertFalse(lessThan("abc").isMoreSpecificThan(lessThanOrEqualTo("abb")));
        assertTrue(lessThan("abc").isMoreSpecificThan(lessThanOrEqualTo("abc")));
        assertTrue(lessThan("abc").isMoreSpecificThan(lessThanOrEqualTo("abd")));

        assertFalse(lessThan(2).isMoreSpecificThan(new Or(greaterThan(1), equalTo(1))));
        assertFalse(lessThan(2).isMoreSpecificThan(new Or(greaterThan(2), equalTo(2))));
        assertFalse(lessThan(2).isMoreSpecificThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(lessThan(2).isMoreSpecificThan(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(lessThan("abc").isMoreSpecificThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertFalse(lessThan("abc").isMoreSpecificThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(lessThan("abc").isMoreSpecificThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(lessThan(2).isMoreSpecificThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(lessThan(2).isMoreSpecificThan(new Or(lessThan(1L), equalTo(1L))));
        assertTrue(lessThan(2).isMoreSpecificThan(new Or(lessThan(2), equalTo(2))));
        assertTrue(lessThan(2).isMoreSpecificThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(lessThan(2).isMoreSpecificThan(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(lessThan("abc").isMoreSpecificThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertTrue(lessThan("abc").isMoreSpecificThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertTrue(lessThan("abc").isMoreSpecificThan(new Or(lessThan("abd"), equalTo("abd"))));
    }

    @Test
    public void shouldCorrectlyJudgeMutuallyExclusive() {
        assertTrue(lessThan(2).isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(lessThan(2).isMutuallyExclusive(undefined()));
        assertFalse(lessThan(2).isMutuallyExclusive(any()));
        assertTrue(lessThan("abc").isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(lessThan("abc").isMutuallyExclusive(undefined()));
        assertFalse(lessThan("abc").isMutuallyExclusive(any()));

        assertFalse(lessThan(2).isMutuallyExclusive(equalTo(1)));
        assertTrue(lessThan(2).isMutuallyExclusive(equalTo(1L)));
        assertTrue(lessThan(2).isMutuallyExclusive(equalTo(2)));
        assertTrue(lessThan(2).isMutuallyExclusive(equalTo(3)));
        assertTrue(lessThan(2).isMutuallyExclusive(equalTo(3L)));
        assertTrue(lessThan(2).isMutuallyExclusive(equalTo(new int[]{3})));
        assertFalse(lessThan("abc").isMutuallyExclusive(equalTo("abb")));
        assertTrue(lessThan("abc").isMutuallyExclusive(equalTo("abc")));
        assertTrue(lessThan("abc").isMutuallyExclusive(equalTo("abd")));

        assertFalse(lessThan(2).isMutuallyExclusive(greaterThan(1)));
        assertTrue(lessThan(2).isMutuallyExclusive(greaterThan(1L)));
        assertTrue(lessThan(2).isMutuallyExclusive(greaterThan(2)));
        assertTrue(lessThan(2).isMutuallyExclusive(greaterThan(3)));
        assertFalse(lessThan("abc").isMutuallyExclusive(greaterThan("abb")));
        assertTrue(lessThan("abc").isMutuallyExclusive(greaterThan("abc")));
        assertTrue(lessThan("abc").isMutuallyExclusive(greaterThan("abd")));

        assertFalse(lessThan(2).isMutuallyExclusive(lessThan(1)));
        assertTrue(lessThan(2).isMutuallyExclusive(lessThan(1L)));
        assertFalse(lessThan(2).isMutuallyExclusive(lessThan(2)));
        assertFalse(lessThan(2).isMutuallyExclusive(lessThan(3)));
        assertTrue(lessThan(2).isMutuallyExclusive(lessThan(3L)));
        assertFalse(lessThan("abc").isMutuallyExclusive(lessThan("abb")));
        assertFalse(lessThan("abc").isMutuallyExclusive(lessThan("abc")));
        assertFalse(lessThan("abc").isMutuallyExclusive(lessThan("abd")));

        assertFalse(lessThan(2).isMutuallyExclusive(greaterThanOrEqualTo(1)));
        assertTrue(lessThan(2).isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertTrue(lessThan(2).isMutuallyExclusive(greaterThanOrEqualTo(3)));
        assertTrue(lessThan(2).isMutuallyExclusive(greaterThanOrEqualTo(3L)));
        assertFalse(lessThan("abc").isMutuallyExclusive(greaterThanOrEqualTo("abb")));
        assertTrue(lessThan("abc").isMutuallyExclusive(greaterThanOrEqualTo("abc")));
        assertTrue(lessThan("abc").isMutuallyExclusive(greaterThanOrEqualTo("abd")));

        assertFalse(lessThan(2).isMutuallyExclusive(lessThanOrEqualTo(1)));
        assertTrue(lessThan(2).isMutuallyExclusive(lessThanOrEqualTo(1L)));
        assertFalse(lessThan(2).isMutuallyExclusive(lessThanOrEqualTo(2)));
        assertFalse(lessThan(2).isMutuallyExclusive(lessThanOrEqualTo(3)));
        assertTrue(lessThan(2).isMutuallyExclusive(lessThanOrEqualTo(3L)));
        assertFalse(lessThan("abc").isMutuallyExclusive(lessThanOrEqualTo("abb")));
        assertFalse(lessThan("abc").isMutuallyExclusive(lessThanOrEqualTo("abc")));
        assertFalse(lessThan("abc").isMutuallyExclusive(lessThanOrEqualTo("abd")));

        assertFalse(lessThan(2).isMutuallyExclusive(new Or(greaterThan(1), equalTo(1))));
        assertTrue(lessThan(2).isMutuallyExclusive(new Or(greaterThan(2), equalTo(2))));
        assertTrue(lessThan(2).isMutuallyExclusive(new Or(greaterThan(3), equalTo(3))));
        assertTrue(lessThan(2).isMutuallyExclusive(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(lessThan("abc").isMutuallyExclusive(new Or(greaterThan("abb"), equalTo("abb"))));
        assertTrue(lessThan("abc").isMutuallyExclusive(new Or(greaterThan("abc"), equalTo("abc"))));
        assertTrue(lessThan("abc").isMutuallyExclusive(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(lessThan(2).isMutuallyExclusive(new Or(lessThan(1), equalTo(1))));
        assertTrue(lessThan(2).isMutuallyExclusive(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(lessThan(2).isMutuallyExclusive(new Or(lessThan(2), equalTo(2))));
        assertFalse(lessThan(2).isMutuallyExclusive(new Or(lessThan(3), equalTo(3))));
        assertTrue(lessThan(2).isMutuallyExclusive(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(lessThan("abc").isMutuallyExclusive(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(lessThan("abc").isMutuallyExclusive(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(lessThan("abc").isMutuallyExclusive(new Or(lessThan("abd"), equalTo("abd"))));
    }
}
