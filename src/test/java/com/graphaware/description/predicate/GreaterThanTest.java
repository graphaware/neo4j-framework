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
 * Unit test for {@link com.graphaware.description.predicate.EqualTo}.
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

        try {
            greaterThan(2).evaluate(UndefinedValue.getInstance());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertTrue(greaterThan(1).isMoreGeneralThan(equalTo(2)));
        assertTrue(greaterThan(0).isMoreGeneralThan(equalTo(2)));
        assertTrue(greaterThan(1).isMoreGeneralThan(greaterThan(2)));
        assertTrue(greaterThan(0).isMoreGeneralThan(greaterThan(2)));
        assertTrue(greaterThan("abc").isMoreGeneralThan(greaterThan("abd")));
        assertTrue(greaterThan("abc").isMoreGeneralThan(greaterThan("aee")));

        assertFalse(greaterThan(1).isMoreGeneralThan(equalTo((short) 2)));
        assertTrue(greaterThan(1).isMoreGeneralThan(greaterThan(2)));
        assertTrue(greaterThan(1).isMoreGeneralThan(greaterThan(1)));
        assertTrue(greaterThan(1L).isMoreGeneralThan(greaterThan(1L)));
        assertFalse(greaterThan(1L).isMoreGeneralThan(greaterThan(1)));
        assertFalse(greaterThan(1).isMoreGeneralThan(greaterThan(1L)));
        assertFalse(greaterThan(1).isMoreGeneralThan(greaterThan(0)));
        assertFalse(greaterThan(1).isMoreGeneralThan(lessThan(2)));
        assertFalse(greaterThan(1).isMoreGeneralThan(lessThan(1)));
        assertFalse(greaterThan(1).isMoreGeneralThan(lessThan(0)));
        assertFalse(greaterThan(1).isMoreGeneralThan(any()));
        assertFalse(greaterThan(1).isMoreGeneralThan(undefined()));
        assertFalse(greaterThan(1).isMoreGeneralThan(new Or(equalTo(0), equalTo(1))));
        assertTrue(greaterThan(1).isMoreGeneralThan(new Or(equalTo(1), equalTo(2))));
        assertFalse(greaterThan(1).isMoreGeneralThan(lessThanOrEqualTo(3)));
        assertTrue(greaterThan(1).isMoreGeneralThan(greaterThanOrEqualTo(2)));
        assertFalse(greaterThan(1).isMoreGeneralThan(greaterThanOrEqualTo(0)));
        assertTrue(greaterThan(1).isMoreGeneralThan(greaterThanOrEqualTo(1)));
    }

    //todo stopped here

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
