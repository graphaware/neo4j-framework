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

/**
 * A {@link ComparablePredicate} checking that beta (the property value) is greater than the predefined value.
 */
final class GreaterThan extends ComparablePredicate {

    /**
     * Construct a new predicate.
     *
     * @param value that beta (the property value) must be greater than to in order for {@link #evaluate(Object)} to
     *              return <code>true</code>.
     */
    GreaterThan(Comparable value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Object beta) {
        checkValueIsLegal(beta);
        return isLessThan(beta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreGeneralThan(Predicate other) {
        if (super.isMoreGeneralThan(other)) {
            return true;
        }

        if (!(other instanceof ValueBasedPredicate)) {
            return false;
        }

        if (other instanceof EqualTo) {
            return isLessThan(((EqualTo) other).getValue());
        }

        if (other instanceof GreaterThan) {
            return isLessThan(((GreaterThan) other).getValue()) || arrayFriendlyEquals(getValue(), ((GreaterThan) other).getValue());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutuallyExclusive(Predicate other) {
        if (super.isMutuallyExclusive(other)) {
            return true;
        }

        if (other instanceof LessThan || other instanceof EqualTo) {
            return !isLessThan(((ValueBasedPredicate) other).getValue());
        }

        if (other instanceof GreaterThan) {
            if (!isGreaterThanOrEqualTo(((ValueBasedPredicate) other).getValue())
                    && !isLessThanOrEqualTo(((ValueBasedPredicate) other).getValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ">" + getValue();
    }
}
