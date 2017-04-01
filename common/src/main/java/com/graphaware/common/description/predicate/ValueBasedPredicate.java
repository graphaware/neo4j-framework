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

import static com.graphaware.common.util.ArrayUtils.arrayFriendlyEquals;
import static com.graphaware.common.util.ArrayUtils.arrayFriendlyHashCode;

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
        return arrayFriendlyHashCode(value);
    }
}
