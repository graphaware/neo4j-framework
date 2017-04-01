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
 * Unit test for {@link com.graphaware.common.description.predicate.GreaterThan}.
 */
public class GreaterThanTest {

    @Test
    public void shouldEvaluateToTrueForGreaterPrimitives() {
        assertTrue(greaterThan((byte) 2).evaluate((byte) 3));
        assertTrue(greaterThan((byte) 2).evaluate((byte) 4));
        assertTrue(greaterThan(new Byte("3")).evaluate((byte) 4));

        assertTrue(greaterThan('e').evaluate('f'));
        assertTrue(greaterThan('e').evaluate('g'));
        assertTrue(greaterThan(new Character('3')).evaluate('4'));

        assertTrue(greaterThan(false).evaluate(true));
        assertTrue(greaterThan(Boolean.FALSE).evaluate(true));

        assertTrue(greaterThan(1).evaluate(2));
        assertTrue(greaterThan(1).evaluate(3));
        assertTrue(greaterThan(new Integer("3")).evaluate(4));

        assertTrue(greaterThan(123L).evaluate(124L));
        assertTrue(greaterThan(123L).evaluate(125L));
        assertTrue(greaterThan(1232384712957129L).evaluate(new Long("1232384712957130")));

        assertTrue(greaterThan((short) 33).evaluate((short) 34));
        assertTrue(greaterThan((short) 33).evaluate((short) 35));
        assertTrue(greaterThan(new Short("3")).evaluate((short) 4));

        assertTrue(greaterThan((float) 3.14).evaluate((float) 3.14001));
        assertTrue(greaterThan(new Float(3.14)).evaluate((float) 3.14001));

        assertTrue(greaterThan(3.14).evaluate(3.14001));
        assertTrue(greaterThan(new Double("3.14")).evaluate(3.14001));
    }

    @Test
    public void shouldEvaluateToFalseForNotGreaterPrimitivesAndPrimitivesOfOtherTypes() {
        assertFalse(greaterThan((byte) 2).evaluate((byte) 2));
        assertFalse(greaterThan((byte) 2).evaluate((byte) 1));
        assertFalse(greaterThan(new Byte("3")).evaluate((byte) 3));
        assertFalse(greaterThan((byte) 2).evaluate(3));
        assertFalse(greaterThan((byte) 2).evaluate(3L));
        assertFalse(greaterThan((byte) 2).evaluate((short) 3));
        assertFalse(greaterThan((byte) 2).evaluate(new byte[]{2, 3}));

        assertFalse(greaterThan('e').evaluate('e'));
        assertFalse(greaterThan('e').evaluate("f"));

        assertFalse(greaterThan(true).evaluate(false));
        assertFalse(greaterThan(Boolean.FALSE).evaluate(false));
        assertFalse(greaterThan(true).evaluate("true"));

        assertFalse(greaterThan(1).evaluate(1));
        assertFalse(greaterThan(1).evaluate(2L));
        assertFalse(greaterThan(1).evaluate((short) 2));
        assertFalse(greaterThan(1).evaluate((byte) 2));
        assertFalse(greaterThan(new Integer("2")).evaluate(2));

        assertFalse(greaterThan(123L).evaluate(123L));
        assertFalse(greaterThan(123L).evaluate(124));
        assertFalse(greaterThan(1232384712957129L).evaluate(new Long("1232384712957129")));

        assertFalse(greaterThan((short) 33).evaluate((byte) 34));
        assertFalse(greaterThan((short) 33).evaluate(34));
        assertFalse(greaterThan((short) 33).evaluate((long) 34));
        assertFalse(greaterThan((short) 33).evaluate((short) 33));
        assertFalse(greaterThan(new Short("3")).evaluate((short) 3));

        assertFalse(greaterThan((float) 3.14).evaluate((float) 3.13999));
        assertFalse(greaterThan(new Float(3.14)).evaluate(3.139));
        assertFalse(greaterThan(new Float(3.14)).evaluate(3));

        assertFalse(greaterThan(3.14).evaluate(3.13999));
        assertFalse(greaterThan(new Double("3.14")).evaluate(3.1399));

        assertFalse(greaterThan(1).evaluate(UndefinedValue.getInstance()));
    }

    @Test
    public void shouldEvaluateToTrueForGreaterString() {
        assertTrue(greaterThan("abc").evaluate("abd"));
        assertTrue(greaterThan("").evaluate("a"));
    }

    @Test
    public void shouldEvaluateToFalseForNotGreaterString() {
        assertFalse(greaterThan("abc").evaluate("abc"));
        assertFalse(greaterThan("abc").evaluate("abb"));
        assertFalse(greaterThan("").evaluate(""));
    }

    @Test
    public void shouldEvaluateToFalseForUndefined() {
        assertFalse(greaterThan("abc").evaluate(UndefinedValue.getInstance()));
        assertFalse(greaterThan(2).evaluate(UndefinedValue.getInstance()));
    }

    @Test
    public void shouldComplainWhenProvidedWithIllegalValues() {
        try {
            greaterThan(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            greaterThan(2).evaluate(new Byte[]{});
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            greaterThan(2).evaluate(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            greaterThan(2).evaluate(new HashMap<>());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertFalse(greaterThan(2).isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThan(2).isMoreGeneralThan(undefined()));
        assertFalse(greaterThan(2).isMoreGeneralThan(any()));
        assertFalse(greaterThan("abc").isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThan("abc").isMoreGeneralThan(undefined()));
        assertFalse(greaterThan("abc").isMoreGeneralThan(any()));

        assertFalse(greaterThan(2).isMoreGeneralThan(equalTo(1)));
        assertFalse(greaterThan(2).isMoreGeneralThan(equalTo(1L)));
        assertFalse(greaterThan(2).isMoreGeneralThan(equalTo(2)));
        assertTrue(greaterThan(2).isMoreGeneralThan(equalTo(3)));
        assertFalse(greaterThan(2).isMoreGeneralThan(equalTo(3L)));
        assertFalse(greaterThan(2).isMoreGeneralThan(equalTo(new int[]{3})));
        assertFalse(greaterThan("abc").isMoreGeneralThan(equalTo("abb")));
        assertFalse(greaterThan("abc").isMoreGeneralThan(equalTo("abc")));
        assertTrue(greaterThan("abc").isMoreGeneralThan(equalTo("abd")));

        assertFalse(greaterThan(2).isMoreGeneralThan(greaterThan(1)));
        assertFalse(greaterThan(2).isMoreGeneralThan(greaterThan(1L)));
        assertTrue(greaterThan(2).isMoreGeneralThan(greaterThan(2)));
        assertTrue(greaterThan(2).isMoreGeneralThan(greaterThan(3)));
        assertFalse(greaterThan("abc").isMoreGeneralThan(greaterThan("abb")));
        assertTrue(greaterThan("abc").isMoreGeneralThan(greaterThan("abc")));
        assertTrue(greaterThan("abc").isMoreGeneralThan(greaterThan("abd")));

        assertFalse(greaterThan(2).isMoreGeneralThan(lessThan(1)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThan(1L)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThan(2)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThan(3)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThan(3L)));
        assertFalse(greaterThan("abc").isMoreGeneralThan(lessThan("abb")));
        assertFalse(greaterThan("abc").isMoreGeneralThan(lessThan("abc")));
        assertFalse(greaterThan("abc").isMoreGeneralThan(lessThan("abd")));

        assertFalse(greaterThan(2).isMoreGeneralThan(greaterThanOrEqualTo(1)));
        assertFalse(greaterThan(2).isMoreGeneralThan(greaterThanOrEqualTo(2)));
        assertTrue(greaterThan(2).isMoreGeneralThan(greaterThanOrEqualTo(3)));
        assertFalse(greaterThan(2).isMoreGeneralThan(greaterThanOrEqualTo(3L)));
        assertFalse(greaterThan("abc").isMoreGeneralThan(greaterThanOrEqualTo("abb")));
        assertFalse(greaterThan("abc").isMoreGeneralThan(greaterThanOrEqualTo("abc")));
        assertTrue(greaterThan("abc").isMoreGeneralThan(greaterThanOrEqualTo("abd")));

        assertFalse(greaterThan(2).isMoreGeneralThan(lessThanOrEqualTo(1)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThanOrEqualTo(1L)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThanOrEqualTo(2)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThanOrEqualTo(3)));
        assertFalse(greaterThan(2).isMoreGeneralThan(lessThanOrEqualTo(3L)));
        assertFalse(greaterThan("abc").isMoreGeneralThan(lessThanOrEqualTo("abb")));
        assertFalse(greaterThan("abc").isMoreGeneralThan(lessThanOrEqualTo("abc")));
        assertFalse(greaterThan("abc").isMoreGeneralThan(lessThanOrEqualTo("abd")));

        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(greaterThan(1), equalTo(1))));
        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(greaterThan(2), equalTo(2))));
        assertTrue(greaterThan(2).isMoreGeneralThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(greaterThan("abc").isMoreGeneralThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertFalse(greaterThan("abc").isMoreGeneralThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertTrue(greaterThan("abc").isMoreGeneralThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(lessThan(2), equalTo(2))));
        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(greaterThan(2).isMoreGeneralThan(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(greaterThan("abc").isMoreGeneralThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(greaterThan("abc").isMoreGeneralThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(greaterThan("abc").isMoreGeneralThan(new Or(lessThan("abd"), equalTo("abd"))));
    }


    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertFalse(greaterThan(2).isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThan(2).isMoreSpecificThan(undefined()));
        assertTrue(greaterThan(2).isMoreSpecificThan(any()));
        assertFalse(greaterThan("abc").isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(greaterThan("abc").isMoreSpecificThan(undefined()));
        assertTrue(greaterThan("abc").isMoreSpecificThan(any()));

        assertFalse(greaterThan(2).isMoreSpecificThan(equalTo(1)));
        assertFalse(greaterThan(2).isMoreSpecificThan(equalTo(1L)));
        assertFalse(greaterThan(2).isMoreSpecificThan(equalTo(2)));
        assertFalse(greaterThan(2).isMoreSpecificThan(equalTo(3)));
        assertFalse(greaterThan(2).isMoreSpecificThan(equalTo(3L)));
        assertFalse(greaterThan(2).isMoreSpecificThan(equalTo(new int[]{3})));
        assertFalse(greaterThan("abc").isMoreSpecificThan(equalTo("abb")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(equalTo("abc")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(equalTo("abd")));

        assertTrue(greaterThan(2).isMoreSpecificThan(greaterThan(1)));
        assertFalse(greaterThan(2).isMoreSpecificThan(greaterThan(1L)));
        assertTrue(greaterThan(2).isMoreSpecificThan(greaterThan(2)));
        assertFalse(greaterThan(2).isMoreSpecificThan(greaterThan(3)));
        assertTrue(greaterThan("abc").isMoreSpecificThan(greaterThan("abb")));
        assertTrue(greaterThan("abc").isMoreSpecificThan(greaterThan("abc")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(greaterThan("abd")));

        assertFalse(greaterThan(2).isMoreSpecificThan(lessThan(1)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThan(1L)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThan(2)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThan(3)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThan(3L)));
        assertFalse(greaterThan("abc").isMoreSpecificThan(lessThan("abb")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(lessThan("abc")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(lessThan("abd")));

        assertTrue(greaterThan(2).isMoreSpecificThan(greaterThanOrEqualTo(1)));
        assertTrue(greaterThan(2).isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(greaterThan(2).isMoreSpecificThan(greaterThanOrEqualTo(3)));
        assertFalse(greaterThan(2).isMoreSpecificThan(greaterThanOrEqualTo(3L)));
        assertTrue(greaterThan("abc").isMoreSpecificThan(greaterThanOrEqualTo("abb")));
        assertTrue(greaterThan("abc").isMoreSpecificThan(greaterThanOrEqualTo("abc")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(greaterThanOrEqualTo("abd")));

        assertFalse(greaterThan(2).isMoreSpecificThan(lessThanOrEqualTo(1)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThanOrEqualTo(1L)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThanOrEqualTo(2)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThanOrEqualTo(3)));
        assertFalse(greaterThan(2).isMoreSpecificThan(lessThanOrEqualTo(3L)));
        assertFalse(greaterThan("abc").isMoreSpecificThan(lessThanOrEqualTo("abb")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(lessThanOrEqualTo("abc")));
        assertFalse(greaterThan("abc").isMoreSpecificThan(lessThanOrEqualTo("abd")));

        assertTrue(greaterThan(2).isMoreSpecificThan(new Or(greaterThan(1), equalTo(1))));
        assertTrue(greaterThan(2).isMoreSpecificThan(new Or(greaterThan(2), equalTo(2))));
        assertFalse(greaterThan(2).isMoreSpecificThan(new Or(greaterThan(3), equalTo(3))));
        assertFalse(greaterThan(2).isMoreSpecificThan(new Or(greaterThan(3L), equalTo(3L))));
        assertTrue(greaterThan("abc").isMoreSpecificThan(new Or(greaterThan("abb"), equalTo("abb"))));
        assertTrue(greaterThan("abc").isMoreSpecificThan(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(greaterThan("abc").isMoreSpecificThan(new Or(greaterThan("abd"), equalTo("abd"))));

        assertFalse(greaterThan(2).isMoreSpecificThan(new Or(lessThan(1), equalTo(1))));
        assertFalse(greaterThan(2).isMoreSpecificThan(new Or(lessThan(1L), equalTo(1L))));
        assertFalse(greaterThan(2).isMoreSpecificThan(new Or(lessThan(2), equalTo(2))));
        assertFalse(greaterThan(2).isMoreSpecificThan(new Or(lessThan(3), equalTo(3))));
        assertFalse(greaterThan(2).isMoreSpecificThan(new Or(lessThan(3L), equalTo(3L))));
        assertFalse(greaterThan("abc").isMoreSpecificThan(new Or(lessThan("abb"), equalTo("abb"))));
        assertFalse(greaterThan("abc").isMoreSpecificThan(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(greaterThan("abc").isMoreSpecificThan(new Or(lessThan("abd"), equalTo("abd"))));
    }

    @Test
    public void shouldCorrectlyJudgeMutuallyExclusive() {
        assertTrue(greaterThan(2).isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(greaterThan(2).isMutuallyExclusive(undefined()));
        assertFalse(greaterThan(2).isMutuallyExclusive(any()));
        assertTrue(greaterThan("abc").isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertTrue(greaterThan("abc").isMutuallyExclusive(undefined()));
        assertFalse(greaterThan("abc").isMutuallyExclusive(any()));

        assertTrue(greaterThan(2).isMutuallyExclusive(equalTo(1)));
        assertTrue(greaterThan(2).isMutuallyExclusive(equalTo(1L)));
        assertTrue(greaterThan(2).isMutuallyExclusive(equalTo(2)));
        assertFalse(greaterThan(2).isMutuallyExclusive(equalTo(3)));
        assertTrue(greaterThan(2).isMutuallyExclusive(equalTo(3L)));
        assertTrue(greaterThan(2).isMutuallyExclusive(equalTo(new int[]{3})));
        assertTrue(greaterThan("abc").isMutuallyExclusive(equalTo("abb")));
        assertTrue(greaterThan("abc").isMutuallyExclusive(equalTo("abc")));
        assertFalse(greaterThan("abc").isMutuallyExclusive(equalTo("abd")));

        assertFalse(greaterThan(2).isMutuallyExclusive(greaterThan(1)));
        assertTrue(greaterThan(2).isMutuallyExclusive(greaterThan(1L)));
        assertFalse(greaterThan(2).isMutuallyExclusive(greaterThan(2)));
        assertFalse(greaterThan(2).isMutuallyExclusive(greaterThan(3)));
        assertFalse(greaterThan("abc").isMutuallyExclusive(greaterThan("abb")));
        assertFalse(greaterThan("abc").isMutuallyExclusive(greaterThan("abc")));
        assertFalse(greaterThan("abc").isMutuallyExclusive(greaterThan("abd")));

        assertTrue(greaterThan(2).isMutuallyExclusive(lessThan(1)));
        assertTrue(greaterThan(2).isMutuallyExclusive(lessThan(1L)));
        assertTrue(greaterThan(2).isMutuallyExclusive(lessThan(2)));
        assertFalse(greaterThan(2).isMutuallyExclusive(lessThan(3)));
        assertTrue(greaterThan(2).isMutuallyExclusive(lessThan(3L)));
        assertTrue(greaterThan("abc").isMutuallyExclusive(lessThan("abb")));
        assertTrue(greaterThan("abc").isMutuallyExclusive(lessThan("abc")));
        assertFalse(greaterThan("abc").isMutuallyExclusive(lessThan("abd")));

        assertFalse(greaterThan(2).isMutuallyExclusive(greaterThanOrEqualTo(1)));
        assertFalse(greaterThan(2).isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertFalse(greaterThan(2).isMutuallyExclusive(greaterThanOrEqualTo(3)));
        assertTrue(greaterThan(2).isMutuallyExclusive(greaterThanOrEqualTo(3L)));
        assertFalse(greaterThan("abc").isMutuallyExclusive(greaterThanOrEqualTo("abb")));
        assertFalse(greaterThan("abc").isMutuallyExclusive(greaterThanOrEqualTo("abc")));
        assertFalse(greaterThan("abc").isMutuallyExclusive(greaterThanOrEqualTo("abd")));

        assertTrue(greaterThan(2).isMutuallyExclusive(lessThanOrEqualTo(1)));
        assertTrue(greaterThan(2).isMutuallyExclusive(lessThanOrEqualTo(1L)));
        assertTrue(greaterThan(2).isMutuallyExclusive(lessThanOrEqualTo(2)));
        assertFalse(greaterThan(2).isMutuallyExclusive(lessThanOrEqualTo(3)));
        assertTrue(greaterThan(2).isMutuallyExclusive(lessThanOrEqualTo(3L)));
        assertTrue(greaterThan("abc").isMutuallyExclusive(lessThanOrEqualTo("abb")));
        assertTrue(greaterThan("abc").isMutuallyExclusive(lessThanOrEqualTo("abc")));
        assertFalse(greaterThan("abc").isMutuallyExclusive(lessThanOrEqualTo("abd")));

        assertFalse(greaterThan(2).isMutuallyExclusive(new Or(greaterThan(1), equalTo(1))));
        assertFalse(greaterThan(2).isMutuallyExclusive(new Or(greaterThan(2), equalTo(2))));
        assertFalse(greaterThan(2).isMutuallyExclusive(new Or(greaterThan(3), equalTo(3))));
        assertTrue(greaterThan(2).isMutuallyExclusive(new Or(greaterThan(3L), equalTo(3L))));
        assertFalse(greaterThan("abc").isMutuallyExclusive(new Or(greaterThan("abb"), equalTo("abb"))));
        assertFalse(greaterThan("abc").isMutuallyExclusive(new Or(greaterThan("abc"), equalTo("abc"))));
        assertFalse(greaterThan("abc").isMutuallyExclusive(new Or(greaterThan("abd"), equalTo("abd"))));

        assertTrue(greaterThan(2).isMutuallyExclusive(new Or(lessThan(1), equalTo(1))));
        assertTrue(greaterThan(2).isMutuallyExclusive(new Or(lessThan(1L), equalTo(1L))));
        assertTrue(greaterThan(2).isMutuallyExclusive(new Or(lessThan(2), equalTo(2))));
        assertFalse(greaterThan(2).isMutuallyExclusive(new Or(lessThan(3), equalTo(3))));
        assertTrue(greaterThan(2).isMutuallyExclusive(new Or(lessThan(3L), equalTo(3L))));
        assertTrue(greaterThan("abc").isMutuallyExclusive(new Or(lessThan("abb"), equalTo("abb"))));
        assertTrue(greaterThan("abc").isMutuallyExclusive(new Or(lessThan("abc"), equalTo("abc"))));
        assertFalse(greaterThan("abc").isMutuallyExclusive(new Or(lessThan("abd"), equalTo("abd"))));
    }
}
