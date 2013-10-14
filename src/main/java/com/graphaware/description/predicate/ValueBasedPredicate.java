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
 * A {@link Predicate} that contains a value and performs some comparison to is when {@link #evaluate(Object)} is invoked.
 */
abstract class ValueBasedPredicate<V> extends BasePredicate {

    private final V value;

    /**
     * Construct a new predicate.
     *
     * @param value contained.
     */
    protected ValueBasedPredicate(V value) {
        checkValueIsLegal(value);
        this.value = value;
    }

    /**
     * Get the value this predicate operates with.
     *
     * @return value.
     */
    protected V getValue() {
        return value;
    }

    /**
     * Check whether two objects, potentially arrays, are equal.
     *
     * @param o1 object 1
     * @param o2 object 2
     * @return true iff o1 and o2 are equal.
     */
    protected boolean arrayFriendlyEquals(Object o1, Object o2) {
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

    /**
     * Compute an array-friendly hash code.
     *
     * @param o object to compute hash code for.
     * @return hash code.
     */
    protected int arrayFriendlyHasCode(Object o) {
        if (o instanceof byte[]) {
            return Arrays.hashCode((byte[]) o);
        } else if (o instanceof char[]) {
            return Arrays.hashCode((char[]) o);
        } else if (o instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) o);
        } else if (o instanceof long[]) {
            return Arrays.hashCode((long[]) o);
        } else if (o instanceof double[]) {
            return Arrays.hashCode((double[]) o);
        } else if (o instanceof int[]) {
            return Arrays.hashCode((int[]) o);
        } else if (o instanceof short[]) {
            return Arrays.hashCode((short[]) o);
        } else if (o instanceof float[]) {
            return Arrays.hashCode((float[]) o);
        } else if (o instanceof Object[]) {
            return Arrays.hashCode((Object[]) o);
        } else {
            return o.hashCode();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueBasedPredicate that = (ValueBasedPredicate) o;

        if (!arrayFriendlyEquals(value, that.value)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return arrayFriendlyHasCode(value);
    }
}
