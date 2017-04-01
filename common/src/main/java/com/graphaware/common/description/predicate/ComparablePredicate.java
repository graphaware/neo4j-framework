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

import com.graphaware.common.log.LoggerFactory;
import org.neo4j.logging.Log;

import static com.graphaware.common.util.ArrayUtils.arrayFriendlyEquals;

/**
 * A {@link Predicate} comparing beta (the property value) to the
 * predefined value. Only compatible with numerical values (in which case number comparison applies), and Strings and
 * chars, in which case alphabetical comparison applies. An exception will be thrown if trying to instantiate this
 * predicate using an array or a non-numerical non-char value.
 */
abstract class ComparablePredicate extends ValueBasedPredicate<Comparable> {

    private static final Log LOG = LoggerFactory.getLogger(ComparablePredicate.class);

    /**
     * Construct a new predicate.
     *
     * @param value to compare.
     */
    protected ComparablePredicate(Comparable value) {
        super(value);
    }

    /**
     * Is the value of this predicate less than the given value?
     *
     * @param value to compare to.
     * @return true iff the value held by this predicate < given value.
     */
    protected final boolean isLessThan(Object value) {
        if (!isComparable(value)) {
            return false;
        }

        try {
            return getValue().compareTo(value) < 0;
        } catch (ClassCastException e) {
            LOG.warn(String.valueOf(getValue()) + " cannot be compared to " + String.valueOf(value));
            return false;
        }
    }

    /**
     * Is the value of this predicate less than or equal to the given value?
     *
     * @param value to compare to.
     * @return true iff the value held by this predicate <= given value.
     */
    protected final boolean isLessThanOrEqualTo(Object value) {
        return isLessThan(value) || arrayFriendlyEquals(getValue(), value);
    }

    /**
     * Is the value of this predicate greater than the given value?
     *
     * @param value to compare to.
     * @return true iff the value held by this predicate > given value.
     */
    protected final boolean isGreaterThan(Object value) {
        if (!isComparable(value)) {
            return false;
        }

        try {
            return getValue().compareTo(value) > 0;
        } catch (ClassCastException e) {
            LOG.warn(String.valueOf(getValue()) + " cannot be compared to " + String.valueOf(value));
            return false;
        }
    }

    /**
     * Is the value of this predicate greater than or equal to the given value?
     *
     * @param value to compare to.
     * @return true iff the value held by this predicate >= given value.
     */
    protected final boolean isGreaterThanOrEqualTo(Object value) {
        return isGreaterThan(value) || arrayFriendlyEquals(getValue(), value);
    }

    /**
     * Is the given value an instance of {@link Comparable}?
     *
     * @param value to check.
     * @return true iff value instanceof Comparable.
     */
    protected final boolean isComparable(Object value) {
        return value instanceof Comparable;
    }
}
