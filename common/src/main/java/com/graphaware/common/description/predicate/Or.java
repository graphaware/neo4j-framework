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
 * A disjunction.
 */
final class Or extends BasePredicate {

    private final Predicate first;
    private final Predicate second;

    /**
     * Construct the predicate.
     *
     * @param first  predicate in the disjunction.
     * @param second predicate in the disjunction.
     */
    Or(Predicate first, Predicate second) {
        this.first = first;
        this.second = second;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Object beta) {
        return first.evaluate(beta) || second.evaluate(beta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMoreGeneralThan(Predicate other) {
        if (super.isMoreGeneralThan(other)) {
            return true;
        }

        return first.isMoreGeneralThan(other) || second.isMoreGeneralThan(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutuallyExclusive(Predicate other) {
        if (super.isMutuallyExclusive(other)) {
            return true;
        }

        return first.isMutuallyExclusive(other) && second.isMutuallyExclusive(other);
    }

    /**
     * Get the first predicate in the disjunction.
     *
     * @return predicate
     */
    Predicate getFirst() {
        return first;
    }

    /**
     * Get the second predicate in the disjunction.
     *
     * @return predicate
     */
    Predicate getSecond() {
        return second;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Or or = (Or) o;

        if (!first.equals(or.first)) return false;
        if (!second.equals(or.second)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getFirst().toString() + "||" + getSecond().toString();
    }
}
