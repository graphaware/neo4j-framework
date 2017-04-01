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
 * Unit test for {@link com.graphaware.common.description.predicate.Undefined}.
 */
public class UndefinedTest {

    @Test
    public void shouldEvaluateToFalseForAllDefinedValues() {
        assertFalse(undefined().evaluate((byte) 2));
        assertFalse(undefined().evaluate('e'));
        assertFalse(undefined().evaluate(true));
        assertFalse(undefined().evaluate(Boolean.FALSE));
        assertFalse(undefined().evaluate(1));
        assertFalse(undefined().evaluate(123L));
        assertFalse(undefined().evaluate(new Long("1232384712957129")));
        assertFalse(undefined().evaluate((short) 33));
        assertFalse(undefined().evaluate((float) 3.14000));
        assertFalse(undefined().evaluate((float) 3.14000));
        assertFalse(undefined().evaluate(3.14000));
        assertFalse(undefined().evaluate("test"));
        assertFalse(undefined().evaluate(""));
        assertTrue(undefined().evaluate(UndefinedValue.getInstance()));

        assertFalse(undefined().evaluate(new byte[]{2, 3, 4}));
        assertFalse(undefined().evaluate(new char[]{'2', 3, '4'}));
        assertFalse(undefined().evaluate(new boolean[]{true, Boolean.FALSE}));
        assertFalse(undefined().evaluate(new int[]{2, 3, 4}));
        assertFalse(undefined().evaluate(new long[]{2L, 3L, 4L}));
        assertFalse(undefined().evaluate(new short[]{2, 3, 4}));
        assertFalse(undefined().evaluate(new float[]{2.15f, 3.1988f, 4.232f}));
        assertFalse(undefined().evaluate(new double[]{2.15, 3.1988, 4.232}));
    }

    @Test
    public void shouldComplainWhenProvidedWithIllegalValues() {
        try {
            undefined().evaluate(new Byte[]{});
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            undefined().evaluate(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            undefined().evaluate(new HashMap<>());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertFalse(undefined().isMoreGeneralThan(equalTo(2)));
        assertTrue(undefined().isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertTrue(undefined().isMoreGeneralThan(undefined()));
        assertFalse(undefined().isMoreGeneralThan(greaterThan(2)));
        assertFalse(undefined().isMoreGeneralThan(lessThan(2)));
        assertFalse(undefined().isMoreGeneralThan(any()));
        assertFalse(undefined().isMoreGeneralThan(new Or(equalTo(2), equalTo(3))));
        assertFalse(undefined().isMoreGeneralThan(lessThanOrEqualTo(2)));
        assertFalse(undefined().isMoreGeneralThan(greaterThanOrEqualTo(2)));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertFalse(undefined().isMoreSpecificThan(equalTo(2)));
        assertTrue(undefined().isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertTrue(undefined().isMoreSpecificThan(undefined()));
        assertFalse(undefined().isMoreSpecificThan(equalTo(3)));
        assertFalse(undefined().isMoreSpecificThan(equalTo(2L)));
        assertFalse(undefined().isMoreSpecificThan(equalTo((short) 2)));
        assertFalse(undefined().isMoreSpecificThan(greaterThan(2)));
        assertFalse(undefined().isMoreSpecificThan(greaterThan(1)));
        assertFalse(undefined().isMoreSpecificThan(lessThan(2)));
        assertFalse(undefined().isMoreSpecificThan(lessThan(3)));
        assertTrue(undefined().isMoreSpecificThan(any()));
        assertFalse(undefined().isMoreSpecificThan(new Or(equalTo(2), equalTo(3))));
        assertFalse(undefined().isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(undefined().isMoreSpecificThan(lessThanOrEqualTo(2)));
        assertFalse(undefined().isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(undefined().isMoreSpecificThan(lessThanOrEqualTo(2)));
    }

    @Test
    public void shouldCorrectlyJudgeMutuallyExclusive() {
        assertTrue(undefined().isMutuallyExclusive(equalTo(2)));
        assertFalse(undefined().isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertFalse(undefined().isMutuallyExclusive(undefined()));
        assertTrue(undefined().isMutuallyExclusive(equalTo(3)));
        assertTrue(undefined().isMutuallyExclusive(equalTo(2L)));
        assertTrue(undefined().isMutuallyExclusive(equalTo((short) 2)));
        assertTrue(undefined().isMutuallyExclusive(greaterThan(2)));
        assertTrue(undefined().isMutuallyExclusive(greaterThan(1)));
        assertTrue(undefined().isMutuallyExclusive(lessThan(2)));
        assertTrue(undefined().isMutuallyExclusive(lessThan(3)));
        assertFalse(undefined().isMutuallyExclusive(any()));
        assertTrue(undefined().isMutuallyExclusive(new Or(equalTo(2), equalTo(3))));
        assertTrue(undefined().isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertTrue(undefined().isMutuallyExclusive(lessThanOrEqualTo(2)));
        assertTrue(undefined().isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertTrue(undefined().isMutuallyExclusive(lessThanOrEqualTo(2)));
    }
}
