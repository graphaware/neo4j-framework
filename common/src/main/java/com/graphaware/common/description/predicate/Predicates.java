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

import com.esotericsoftware.kryo.Kryo;

/**
 * Utility class acting as a factory for {@link Predicate}s.
 */
public final class Predicates {

    private Predicates() {
    }

    /**
     * Create an {@link Any} predicate.
     *
     * @return predicate.
     */
    public static Predicate any() {
        return Any.getInstance();
    }

    /**
     * Create an {@link Undefined} predicate.
     *
     * @return predicate.
     */
    public static Predicate undefined() {
        return Undefined.getInstance();
    }

    /**
     * Create an {@link EqualTo} predicate with the specified value.
     *
     * @param value of the predicate.
     * @return predicate.
     */
    public static Predicate equalTo(Object value) {
        return new EqualTo(value);
    }

    /**
     * Create a {@link GreaterThan} predicate with the specified value.
     *
     * @param value of the predicate.
     * @return predicate.
     */
    public static Predicate greaterThan(Comparable value) {
        return new GreaterThan(value);
    }

    /**
     * Create a {@link GreaterThan} or {@link EqualTo} predicate with the specified value.
     *
     * @param value of the predicate.
     * @return predicate.
     */
    public static Predicate greaterThanOrEqualTo(Comparable value) {
        return new Or(new GreaterThan(value), new EqualTo(value));
    }

    /**
     * Create a {@link LessThan} predicate with the specified value.
     *
     * @param value of the predicate.
     * @return predicate.
     */
    public static Predicate lessThan(Comparable value) {
        return new LessThan(value);
    }

    /**
     * Create a {@link LessThan} or {@link EqualTo} predicate with the specified value.
     *
     * @param value of the predicate.
     * @return predicate.
     */
    public static Predicate lessThanOrEqualTo(Comparable value) {
        return new Or(new LessThan(value), new EqualTo(value));
    }

    /**
     * Register all concrete predicates with kryo.
     *
     * @param kryo to register with.
     */
    public static void register(Kryo kryo) {
        kryo.register(Any.class, 20);
        kryo.register(EqualTo.class, 21);
        kryo.register(GreaterThan.class, 22);
        kryo.register(LessThan.class, 23);
        kryo.register(Or.class, 24);
        kryo.register(Undefined.class, 25);
        kryo.register(UndefinedValue.class, 26);
    }
}
