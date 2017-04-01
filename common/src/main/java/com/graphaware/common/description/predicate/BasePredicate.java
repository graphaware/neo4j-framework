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

import com.graphaware.common.description.BasePartiallyComparable;

import static com.graphaware.common.util.ArrayUtils.isPrimitiveOrStringArray;

/**
 * Base-class for {@link Predicate} implementations.
 */
abstract class BasePredicate extends BasePartiallyComparable<Predicate> implements Predicate {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Predicate self() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreGeneralThan(Predicate other) {
        if (other instanceof Or) {
            return isMoreGeneralThan(((Or) other).getFirst()) && isMoreGeneralThan(((Or) other).getSecond());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutuallyExclusive(Predicate other) {
        if (other instanceof Or) {
            return isMutuallyExclusive(((Or) other).getFirst()) && isMutuallyExclusive(((Or) other).getSecond());
        }
        return false;
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
}
