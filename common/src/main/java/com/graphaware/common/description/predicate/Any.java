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

/**
 * A {@link Predicate} representing a wildcard, i.e., always returning true from {@link #evaluate(Object)} no matter
 * the argument. Singleton.
 */
final class Any extends BasePredicate {

    private static final Any INSTANCE = new Any();

    private Any() {
    }

    /**
     * Get an instance of this predicate.
     *
     * @return an instance.
     */
    static Any getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Object beta) {
        checkValueIsLegal(beta);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreGeneralThan(Predicate other) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutuallyExclusive(Predicate other) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return "any".hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "=*";
    }
}
