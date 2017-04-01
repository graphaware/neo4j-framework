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
 * Unit test for {@link com.graphaware.common.description.predicate.Or}.
 */
public class OrTest {

    @Test
    public void shouldEvaluateToTrueForGreaterOrEqualPrimitives() {
        assertTrue(greaterThanOrEqualTo((byte) 2).evaluate((byte) 2));
        assertTrue(greaterThanOrEqualTo((byte) 2).evaluate((byte) 3));
        assertTrue(greaterThanOrEqualTo((byte) 2).evaluate((byte) 4));
        assertTrue(greaterThanOrEqualTo(new Byte("3")).evaluate((byte) 3));
        assertTrue(greaterThanOrEqualTo(new Byte("3")).evaluate((byte) 4));

        assertTrue(greaterThanOrEqualTo('e').evaluate('e'));
        assertTrue(greaterThanOrEqualTo('e').evaluate('f'));
        assertTrue(greaterThanOrEqualTo('e').evaluate('g'));
        assertTrue(greaterThanOrEqualTo(new Character('3')).evaluate('4'));

        assertTrue(greaterThanOrEqualTo(Boolean.FALSE).evaluate(false));
        assertTrue(greaterThanOrEqualTo(false).evaluate(true));
        assertTrue(greaterThanOrEqualTo(Boolean.FALSE).evaluate(true));

        assertTrue(greaterThanOrEqualTo(1).evaluate(1));
        assertTrue(greaterThanOrEqualTo(1).evaluate(2));
        assertTrue(greaterThanOrEqualTo(1).evaluate(3));
        assertTrue(greaterThanOrEqualTo(new Integer("2")).evaluate(2));
        assertTrue(greaterThanOrEqualTo(new Integer("3")).evaluate(4));

        assertTrue(greaterThanOrEqualTo(123L).evaluate(123L));
        assertTrue(greaterThanOrEqualTo(123L).evaluate(124L));
        assertTrue(greaterThanOrEqualTo(123L).evaluate(125L));
        assertTrue(greaterThanOrEqualTo(1232384712957129L).evaluate(new Long("1232384712957130")));
        assertTrue(greaterThanOrEqualTo(1232384712957129L).evaluate(new Long("1232384712957129")));

        assertTrue(greaterThanOrEqualTo((short) 33).evaluate((short) 33));
        assertTrue(greaterThanOrEqualTo(new Short("3")).evaluate((short) 3));
        assertTrue(greaterThanOrEqualTo((short) 33).evaluate((short) 34));
        assertTrue(greaterThanOrEqualTo((short) 33).evaluate((short) 35));
        assertTrue(greaterThanOrEqualTo(new Short("3")).evaluate((short) 4));

        assertTrue(greaterThanOrEqualTo((float) 3.14).evaluate((float) 3.14001));
        assertTrue(greaterThanOrEqualTo(new Float(3.14)).evaluate((float) 3.14001));

        assertTrue(greaterThanOrEqualTo(3.14).evaluate(3.14001));
        assertTrue(greaterThanOrEqualTo(new Double("3.14")).evaluate(3.14001));
    }

    @Test
    public void shouldEvaluateToFalseForSmallerPrimitivesAndPrimitivesOfOtherTypes() {
        assertFalse(greaterThanOrEqualTo((byte) 2).evaluate((byte) 1));
        assertFalse(greaterThanOrEqualTo((byte) 2).evaluate(3));
        assertFalse(greaterThanOrEqualTo((byte) 2).evaluate(3L));
        assertFalse(greaterThanOrEqualTo((byte) 2).evaluate((short) 3));
        assertFalse(greaterThanOrEqualTo((byte) 2).evaluate(new byte[]{2, 3}));

        assertFalse(greaterThanOrEqualTo('e').evaluate("f"));

        assertFalse(greaterThanOrEqualTo(true).evaluate(false));
        assertFalse(greaterThanOrEqualTo(true).evaluate("true"));

        assertFalse(greaterThanOrEqualTo(1).evaluate(2L));
        assertFalse(greaterThanOrEqualTo(1).evaluate((short) 2));
        assertFalse(greaterThanOrEqualTo(1).evaluate((byte) 2));

        assertFalse(greaterThanOrEqualTo(123L).evaluate(124));

        assertFalse(greaterThanOrEqualTo((short) 33).evaluate((byte) 34));
        assertFalse(greaterThanOrEqualTo((short) 33).evaluate(34));
        assertFalse(greaterThanOrEqualTo((short) 33).evaluate((long) 34));

        assertFalse(greaterThanOrEqualTo((float) 3.14).evaluate((float) 3.13999));
        assertFalse(greaterThanOrEqualTo(new Float(3.14)).evaluate(3.139));
        assertFalse(greaterThanOrEqualTo(new Float(3.14)).evaluate(3));

        assertFalse(greaterThanOrEqualTo(3.14).evaluate(3.13999));
        assertFalse(greaterThanOrEqualTo(new Double("3.14")).evaluate(3.1399));

        assertFalse(greaterThanOrEqualTo(1).evaluate(UndefinedValue.getInstance()));
    }

    @Test
    public void shouldEvaluateToTrueForGreaterOrEqualString() {
        assertTrue(greaterThanOrEqualTo("abc").evaluate("abc"));
        assertTrue(greaterThanOrEqualTo("abc").evaluate("abd"));
        assertTrue(greaterThanOrEqualTo("").evaluate("a"));
        assertTrue(greaterThanOrEqualTo("").evaluate(""));
    }

    @Test
    public void shouldEvaluateToFalseForLesserString() {
        assertFalse(greaterThanOrEqualTo("abc").evaluate("abb"));
    }

    @Test
    public void shouldEvaluateToFalseForUndefined() {
        assertFalse(greaterThanOrEqualTo("abc").evaluate(UndefinedValue.getInstance()));
        assertFalse(greaterThanOrEqualTo(2).evaluate(UndefinedValue.getInstance()));
    }

    @Test
    public void shouldComplainWhenProvidedWithIllegalValues() {
        try {
            greaterThanOrEqualTo(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            greaterThanOrEqualTo(2).evaluate(new Byte[]{});
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            greaterThanOrEqualTo(2).evaluate(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            greaterThanOrEqualTo(2).evaluate(new HashMap<>());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(undefined()));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(any()));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(undefined()));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(any()));

        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(equalTo(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(equalTo(1L)));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(equalTo(2)));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(equalTo(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(equalTo(3L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(equalTo(new int[]{3})));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(equalTo("abb")));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(equalTo("abc")));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(equalTo("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThan(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThan(1L)));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThan(2)));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThan(3)));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(greaterThan("abb")));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(greaterThan("abc")));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(greaterThan("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThan(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThan(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThan(2)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThan(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThan(3L)));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(lessThan("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(lessThan("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(lessThan("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThanOrEqualTo(1)));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThanOrEqualTo(2)));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThanOrEqualTo(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(greaterThanOrEqualTo(3L)));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(greaterThanOrEqualTo("abb")));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(greaterThanOrEqualTo("abc")));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(greaterThanOrEqualTo("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThanOrEqualTo(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThanOrEqualTo(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThanOrEqualTo(2)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThanOrEqualTo(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(lessThanOrEqualTo(3L)));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(lessThanOrEqualTo("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(lessThanOrEqualTo("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(lessThanOrEqualTo("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(greaterThan(1), equalTo(1))));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(greaterThan(2), equalTo(2))));
        assertTrue(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertTrue(greaterThanOrEqualTo("abc").isMoreGeneralThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(lessThan(2), equalTo(2))));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(greaterThanOrEqualTo(2).isMoreGeneralThan(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreGeneralThan(new Or(lessThan("abd"), equalTo("abd"))));
    }


    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(undefined()));
        assertTrue(greaterThanOrEqualTo(2).isMoreSpecificThan(any()));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(undefined()));
        assertTrue(greaterThanOrEqualTo("abc").isMoreSpecificThan(any()));

        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(equalTo(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(equalTo(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(equalTo(2)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(equalTo(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(equalTo(3L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(equalTo(new int[]{3})));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(equalTo("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(equalTo("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(equalTo("abd")));

        assertTrue(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThan(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThan(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThan(2)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThan(3)));
        assertTrue(greaterThanOrEqualTo("abc").isMoreSpecificThan(greaterThan("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(greaterThan("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(greaterThan("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThan(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThan(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThan(2)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThan(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThan(3L)));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(lessThan("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(lessThan("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(lessThan("abd")));

        assertTrue(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThanOrEqualTo(1)));
        assertTrue(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThanOrEqualTo(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(greaterThanOrEqualTo(3L)));
        assertTrue(greaterThanOrEqualTo("abc").isMoreSpecificThan(greaterThanOrEqualTo("abb")));
        assertTrue(greaterThanOrEqualTo("abc").isMoreSpecificThan(greaterThanOrEqualTo("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(greaterThanOrEqualTo("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThanOrEqualTo(1)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThanOrEqualTo(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThanOrEqualTo(2)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThanOrEqualTo(3)));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(lessThanOrEqualTo(3L)));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(lessThanOrEqualTo("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(lessThanOrEqualTo("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(lessThanOrEqualTo("abd")));

        assertTrue(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(greaterThan(1), equalTo(1))));
        assertTrue(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(greaterThan(2), equalTo(2))));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(greaterThan(3L), equalTo(3L))));
        assertTrue(greaterThanOrEqualTo("abc").isMoreSpecificThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertTrue(greaterThanOrEqualTo("abc").isMoreSpecificThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(lessThan(2), equalTo(2))));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(greaterThanOrEqualTo(2).isMoreSpecificThan(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(greaterThanOrEqualTo("abc").isMoreSpecificThan(new Or(lessThan("abd"), equalTo("abd"))));
    }

    @Test
    public void shouldCorrectlyJudgeMutuallyExclusive() {
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(undefined()));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(any()));
        assertTrue(greaterThanOrEqualTo("abc").isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(greaterThanOrEqualTo("abc").isMutuallyExclusive(undefined()));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(any()));

        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(equalTo(1)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(equalTo(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(equalTo(2)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(equalTo(3)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(equalTo(3L)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(equalTo(new int[]{3})));
        assertTrue(greaterThanOrEqualTo("abc").isMutuallyExclusive(equalTo("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(equalTo("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(equalTo("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThan(1)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThan(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThan(2)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThan(3)));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(greaterThan("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(greaterThan("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(greaterThan("abd")));

        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThan(1)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThan(1L)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThan(2)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThan(3)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThan(3L)));
        assertTrue(greaterThanOrEqualTo("abc").isMutuallyExclusive(lessThan("abb")));
        assertTrue(greaterThanOrEqualTo("abc").isMutuallyExclusive(lessThan("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(lessThan("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThanOrEqualTo(1)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThanOrEqualTo(3)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(greaterThanOrEqualTo(3L)));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(greaterThanOrEqualTo("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(greaterThanOrEqualTo("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(greaterThanOrEqualTo("abd")));

        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThanOrEqualTo(1)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThanOrEqualTo(1L)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThanOrEqualTo(2)));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThanOrEqualTo(3)));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(lessThanOrEqualTo(3L)));
        assertTrue(greaterThanOrEqualTo("abc").isMutuallyExclusive(lessThanOrEqualTo("abb")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(lessThanOrEqualTo("abc")));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(lessThanOrEqualTo("abd")));

        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(greaterThan(1), equalTo(1))));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(greaterThan(2), equalTo(2))));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(greaterThan(3), equalTo(3))));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(new Or(greaterThan("abb"), equalTo("abb"))));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(new Or(greaterThan("abd"), equalTo("abd"))));

        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(lessThan(1), equalTo(1))));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(lessThan(2), equalTo(2))));
        assertFalse(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(lessThan(3), equalTo(3))));
        assertTrue(greaterThanOrEqualTo(2).isMutuallyExclusive(new Or(lessThan(3L), equalTo(3L))));
        assertTrue(greaterThanOrEqualTo("abc").isMutuallyExclusive(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(greaterThanOrEqualTo("abc").isMutuallyExclusive(new Or(lessThan("abd"), equalTo("abd"))));
    }
}
