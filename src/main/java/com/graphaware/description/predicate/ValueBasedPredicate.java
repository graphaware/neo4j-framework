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

/**
 *
 */
abstract class ValueBasedPredicate<V> extends BasePredicate {

    private final V value;

    protected ValueBasedPredicate(V value) {
        checkValueIsLegal(value);
        this.value = value;
    }

    protected V getValue() {
        return value;
    }

    /**
     * Check that the value is legal, i.e., a primitive, a String, an array of primitives, or an array of Strings.
     *
     * @param value to check.
     * @throws IllegalArgumentException in case the value is illegal.
     */
    protected final void checkValueIsLegal(Object value) {
        if (!isPrimitiveOrString(value) && !isPrimitiveOrStringArray(value) && !UndefinedValue.getInstance().equals(value)) {
            throw new IllegalArgumentException("Value must be a primitive, a String, an array of primitives, or an array of Strings");
        }
    }

    /**
     * Check if the given object is a primitive array.
     *
     * @param o to check.
     * @return true iff o is a primitive array.
     */
    private boolean isPrimitiveArray(Object o) {
        if (o instanceof byte[]) {
            return true;
        } else if (o instanceof char[]) {
            return true;
        } else if (o instanceof boolean[]) {
            return true;
        } else if (o instanceof long[]) {
            return true;
        } else if (o instanceof double[]) {
            return true;
        } else if (o instanceof int[]) {
            return true;
        } else if (o instanceof short[]) {
            return true;
        } else if (o instanceof float[]) {
            return true;
        }
        return false;
    }

    /**
     * Check if the given object is a primitive array or an array of Strings.
     *
     * @param o to check.
     * @return true iff o is a primitive array or an array of Strings.
     */
    private boolean isPrimitiveOrStringArray(Object o) {
        if (isPrimitiveArray(o)) {
            return true;
        } else if (o instanceof String[]) {
            return true;
        }
        return false;
    }

    /**
     * Check if the given object is a primitive or a String.
     *
     * @param o to check.
     * @return true iff o is a primitive or a of String.
     */
    private boolean isPrimitiveOrString(Object o) {
        if (o instanceof Byte) {
            return true;
        } else if (o instanceof Character) {
            return true;
        } else if (o instanceof Boolean) {
            return true;
        } else if (o instanceof Long) {
            return true;
        } else if (o instanceof Double) {
            return true;
        } else if (o instanceof Integer) {
            return true;
        } else if (o instanceof Short) {
            return true;
        } else if (o instanceof Float) {
            return true;
        } else if (o instanceof String) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueBasedPredicate that = (ValueBasedPredicate) o;

        if (!value.equals(that.value)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
