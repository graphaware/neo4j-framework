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
 * Unit test for {@link com.graphaware.common.description.predicate.Any}.
 */
public class AnyTest {

    @Test
    public void shouldEvaluateToTrueForAllValues() {
        assertTrue(any().evaluate((byte) 2));
        assertTrue(any().evaluate('e'));
        assertTrue(any().evaluate(true));
        assertTrue(any().evaluate(Boolean.FALSE));
        assertTrue(any().evaluate(1));
        assertTrue(any().evaluate(123L));
        assertTrue(any().evaluate(new Long("1232384712957129")));
        assertTrue(any().evaluate((short) 33));
        assertTrue(any().evaluate((float) 3.14000));
        assertTrue(any().evaluate((float) 3.14000));
        assertTrue(any().evaluate(3.14000));
        assertTrue(any().evaluate("test"));
        assertTrue(any().evaluate(""));
        assertTrue(any().evaluate(UndefinedValue.getInstance()));

        assertTrue(any().evaluate(new byte[]{2, 3, 4}));
        assertTrue(any().evaluate(new char[]{'2', 3, '4'}));
        assertTrue(any().evaluate(new boolean[]{true, Boolean.FALSE}));
        assertTrue(any().evaluate(new int[]{2, 3, 4}));
        assertTrue(any().evaluate(new long[]{2L, 3L, 4L}));
        assertTrue(any().evaluate(new short[]{2, 3, 4}));
        assertTrue(any().evaluate(new float[]{2.15f, 3.1988f, 4.232f}));
        assertTrue(any().evaluate(new double[]{2.15, 3.1988, 4.232}));
    }

    @Test
    public void shouldComplainWhenProvidedWithIllegalValues() {
        try {
            any().evaluate(new Byte[]{});
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            any().evaluate(null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            any().evaluate(new HashMap<>());
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void shouldCorrectlyJudgeMoreGeneral() {
        assertTrue(any().isMoreGeneralThan(equalTo(2)));
        assertTrue(any().isMoreGeneralThan(equalTo(UndefinedValue.getInstance())));
        assertTrue(any().isMoreGeneralThan(undefined()));
        assertTrue(any().isMoreGeneralThan(greaterThan(2)));
        assertTrue(any().isMoreGeneralThan(lessThan(2)));
        assertTrue(any().isMoreGeneralThan(any()));
        assertTrue(any().isMoreGeneralThan(new Or(equalTo(2), equalTo(3))));
        assertTrue(any().isMoreGeneralThan(lessThanOrEqualTo(2)));
        assertTrue(any().isMoreGeneralThan(greaterThanOrEqualTo(2)));
    }

    @Test
    public void shouldCorrectlyJudgeMoreSpecific() {
        assertFalse(any().isMoreSpecificThan(equalTo(2)));
        assertFalse(any().isMoreSpecificThan(equalTo(UndefinedValue.getInstance())));
        assertFalse(any().isMoreSpecificThan(undefined()));
        assertFalse(any().isMoreSpecificThan(equalTo(3)));
        assertFalse(any().isMoreSpecificThan(equalTo(2L)));
        assertFalse(any().isMoreSpecificThan(equalTo((short) 2)));
        assertFalse(any().isMoreSpecificThan(greaterThan(2)));
        assertFalse(any().isMoreSpecificThan(greaterThan(1)));
        assertFalse(any().isMoreSpecificThan(lessThan(2)));
        assertFalse(any().isMoreSpecificThan(lessThan(3)));
        assertTrue(any().isMoreSpecificThan(any()));
        assertFalse(any().isMoreSpecificThan(new Or(equalTo(2), equalTo(3))));
        assertFalse(any().isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(any().isMoreSpecificThan(lessThanOrEqualTo(2)));
        assertFalse(any().isMoreSpecificThan(greaterThanOrEqualTo(2)));
        assertFalse(any().isMoreSpecificThan(lessThanOrEqualTo(2)));
    }

    @Test
    public void shouldCorrectlyJudgeMutuallyExclusive() {
        assertFalse(any().isMutuallyExclusive(equalTo(2)));
        assertFalse(any().isMutuallyExclusive(equalTo(UndefinedValue.getInstance())));
        assertFalse(any().isMutuallyExclusive(undefined()));
        assertFalse(any().isMutuallyExclusive(equalTo(3)));
        assertFalse(any().isMutuallyExclusive(equalTo(2L)));
        assertFalse(any().isMutuallyExclusive(equalTo((short) 2)));
        assertFalse(any().isMutuallyExclusive(greaterThan(2)));
        assertFalse(any().isMutuallyExclusive(greaterThan(1)));
        assertFalse(any().isMutuallyExclusive(lessThan(2)));
        assertFalse(any().isMutuallyExclusive(lessThan(3)));
        assertFalse(any().isMutuallyExclusive(any()));
        assertFalse(any().isMutuallyExclusive(new Or(equalTo(2), equalTo(3))));
        assertFalse(any().isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertFalse(any().isMutuallyExclusive(lessThanOrEqualTo(2)));
        assertFalse(any().isMutuallyExclusive(greaterThanOrEqualTo(2)));
        assertFalse(any().isMutuallyExclusive(lessThanOrEqualTo(2)));
    }
}
