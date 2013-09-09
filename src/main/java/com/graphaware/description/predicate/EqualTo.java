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

import java.util.Arrays;

/**
 * A {@link Predicate} checking that beta (the property value) is equal to a predefined value.
 */
class EqualTo extends ValueBasedPredicate<Object> {

    /**
     * Construct a new predicate.
     *
     * @param value that beta (the property value) must be equal to in order for {@link #evaluate(Object)} to return
     *              <code>true</code>.
     */
    EqualTo(Object value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Object beta) {
        return arrayFriendlyEquals(getValue(), beta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreGeneralThan(Predicate other) {
        return false;
    }

    /**
     * Check whether two objects, potentially arrays, are equal.
     *
     * @param o1 object 1
     * @param o2 object 2
     * @return true iff o1 and o2 are equal.
     */
    private boolean arrayFriendlyEquals(Object o1, Object o2) {
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals(((byte[]) o1), (byte[]) o2);
        } else if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals(((char[]) o1), (char[]) o2);
        } else if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals(((boolean[]) o1), (boolean[]) o2);
        } else if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals(((long[]) o1), (long[]) o2);
        } else if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals(((double[]) o1), (double[]) o2);
        } else if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals(((int[]) o1), (int[]) o2);
        } else if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals(((short[]) o1), (short[]) o2);
        } else if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals(((float[]) o1), (float[]) o2);
        } else if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals(((Object[]) o1), (Object[]) o2);
        } else return o1.equals(o2);
    }
}
