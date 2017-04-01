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

package com.graphaware.common.util;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.helpers.collection.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utility methods for dealing with {@link Iterable}s.
 */
public final class IterableUtils {

    /**
     * Count all nodes in a database.
     * <p/>
     * Please note that this can be an expensive operation! As such, it is intended mainly for testing.
     *
     * @param database in which to count nodes.
     * @return all nodes in the database (including root with ID 0, i.e. an brand new database will have 1 node).
     */
    public static long countNodes(GraphDatabaseService database) {
        return count(database.getAllNodes());
    }

    /**
     * Count items in an iterable.
     *
     * @param iterable to count items in.
     * @return number of items in the iterable.
     */
    public static long count(Iterable iterable) {
        if (iterable instanceof Collection) {
            return ((Collection) iterable).size();
        }

        return Iterables.count(iterable);
    }

    /**
     * Check whether an iterable contains the given object.
     *
     * @param iterable to check in.
     * @param object   to look for.
     * @param <T>      type of the objects stored in the iterable.
     * @return true iff the object is contained in the iterable.
     */
    public static <T> boolean contains(Iterable<T> iterable, T object) {
        if (iterable instanceof Collection) {
            return ((Collection) iterable).contains(object);
        }

        for (T t : iterable) {
            if (t.equals(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert an iterable to a list.
     *
     * @param iterable to convert.
     * @param <T>      type of the items held.
     * @return a list.
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();

        if (iterable instanceof Collection) {
            list.addAll((Collection<T>) iterable);
        } else {
            for (T next : iterable) {
                list.add(next);
            }
        }

        return list;
    }

    /**
     * Get a random element of the given {@link Iterable}.  This is functionally equivalent to calling
     * {@link #random(Iterable, int)} with a sample size of 1.
     *
     * @param iterable The {@link Iterable} from which to get a random element.
     * @param <T>      The type of the element, as dictated by the given argument.
     * @return A random element from the given iterable.
     * @throws IllegalArgumentException if invoked with an empty {@link Iterable}.
     */
    public static <T> T random(Iterable<T> iterable) {
        return random(iterable, 1).iterator().next();
    }

    /**
     * Sample an {@link Iterable} using reservoir sampling.
     *
     * @param iterable        The {@link Iterable} from which to sample.
     * @param numberOfSamples The number of elements to return.
     * @param <T>             The type of the element, as dictated by the given {@link Iterable}.
     * @return A new {@link Iterable} containing a number of random elements sampled from the {@link Iterable} argument.
     * @throws IllegalArgumentException if invoked with an empty {@link Iterable}.
     */
    public static <T> Iterable<T> random(Iterable<T> iterable, int numberOfSamples) {
        if (!iterable.iterator().hasNext()) {
            throw new IllegalArgumentException("Empty iterable can't be randomized!");
        }

        ReservoirSampler<T> sampler = new ReservoirSampler<>(numberOfSamples);
        for (T item : iterable) {
            sampler.sample(item);
        }
        return sampler.getSamples();
    }

    /**
     * Get a single element from iterator.
     *
     * @param iterator        to find a single element.
     * @param notFoundMessage exception message if there are no elements.
     * @param <T>             type of the element.
     * @return the element iff there is exactly one.
     * @throws NotFoundException     in case there are no elements.
     * @throws IllegalStateException in case the iterable contains more than 1 element.
     */
    public static <T> T getSingle(Iterator<T> iterator, String notFoundMessage) {
        T result = getSingleOrNull(iterator);

        if (result == null) {
            throw new NotFoundException(notFoundMessage);
        }

        return result;
    }

    /**
     * Get a single element from iterable.
     *
     * @param iterable        to find a single element.
     * @param notFoundMessage exception message if there are no elements.
     * @param <T>             type of the element.
     * @return the element iff there is exactly one.
     * @throws NotFoundException     in case there are no elements.
     * @throws IllegalStateException in case the iterable contains more than 1 element.
     */
    public static <T> T getSingle(Iterable<T> iterable, String notFoundMessage) {
        return getSingle(iterable.iterator(), notFoundMessage);
    }

    /**
     * Get a single element from iterator.
     *
     * @param iterator to find a single element.
     * @param <T>      type of the element.
     * @return the element iff there is exactly one.
     * @throws NotFoundException     in case there are no elements.
     * @throws IllegalStateException in case the iterable contains more than 1 element.
     */
    public static <T> T getSingle(Iterator<T> iterator) {
        return getSingle(iterator, "Iterator is empty");
    }

    /**
     * Get a single element from iterable.
     *
     * @param iterable to find a single element.
     * @param <T>      type of the element.
     * @return the element iff there is exactly one.
     * @throws NotFoundException     in case there are no elements.
     * @throws IllegalStateException in case the iterable contains more than 1 element.
     */
    public static <T> T getSingle(Iterable<T> iterable) {
        return getSingle(iterable.iterator(), "Iterable is empty");
    }

    /**
     * Get a single element from iterator.
     *
     * @param iterator to find a single element.
     * @param <T>      type of the element.
     * @return the element iff there is exactly one, null iff there is 0.
     * @throws IllegalStateException in case the iterable contains more than 1 element.
     */
    public static <T> T getSingleOrNull(Iterator<T> iterator) {
        T result = null;

        if (iterator.hasNext()) {
            result = iterator.next();
        }

        if (iterator.hasNext()) {
            throw new IllegalStateException("Iterable has more than one element, which is unexpected");
        }

        return result;
    }

    /**
     * Get a single element from iterable.
     *
     * @param iterable to find a single element.
     * @param <T>      type of the element.
     * @return the element iff there is exactly one, null iff there is 0.
     * @throws IllegalStateException in case the iterable contains more than 1 element.
     */
    public static <T> T getSingleOrNull(Iterable<T> iterable) {
        return getSingleOrNull(iterable.iterator());
    }

    /**
     * Get the first element from iterator.
     *
     * @param iterator        to find the first element.
     * @param notFoundMessage exception message if there are no elements.
     * @param <T>             type of the element.
     * @return the element iff there is one or more.
     * @throws NotFoundException in case there are no elements.
     */
    public static <T> T getFirst(Iterator<T> iterator, String notFoundMessage) {
        T result = null;

        if (iterator.hasNext()) {
            result = iterator.next();
        }

        if (result == null) {
            throw new NotFoundException(notFoundMessage);
        }

        return result;
    }

    /**
     * Get the first element from iterable.
     *
     * @param iterable        to find the first element.
     * @param notFoundMessage exception message if there are no elements.
     * @param <T>             type of the element.
     * @return the element iff there is one or more.
     * @throws NotFoundException in case there are no elements.
     */
    public static <T> T getFirst(Iterable<T> iterable, String notFoundMessage) {
        return getFirst(iterable.iterator(), notFoundMessage);
    }

    /**
     * Get the first element from iterator.
     *
     * @param iterator        to find the first element.
     * @param <T>             type of the element.
     * @return the element iff there is one or more, null if there is none.
     */
    public static <T> T getFirstOrNull(Iterator<T> iterator) {
        T result = null;

        if (iterator.hasNext()) {
            result = iterator.next();
        }

        return result;
    }

    /**
     * Get the first element from iterable.
     *
     * @param iterable        to find the first element.
     * @param <T>             type of the element.
     * @return the element iff there is one or more, null if there is none.
     */
    public static <T> T getFirstOrNull(Iterable<T> iterable) {
        return getFirstOrNull(iterable.iterator());
    }

    /**
     * private constructor to prevent instantiation.
     */
    private IterableUtils() {
    }
}
