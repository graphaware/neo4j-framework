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

package com.graphaware.test;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    public static int countNodes(GraphDatabaseService database) {
        return count(GlobalGraphOperations.at(database).getAllNodes());
    }

    /**
     * Count items in an iterable.
     *
     * @param iterable to count items in.
     * @return number of items in the iterable.
     */
    public static int count(Iterable iterable) {
        if (iterable instanceof Collection) {
            return ((Collection) iterable).size();
        }

        int result = 0;
        for (Object ignored : iterable) {
            result++;
        }
        return result;
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
     * Get a random element of the given iterable.
     *
     * @param iterable to get a random element from.
     * @param <T>      type of the element.
     * @return random element.
     */
    public static <T> T random(Iterable<T> iterable) {
        if (!iterable.iterator().hasNext()) {
            throw new IllegalArgumentException("Empty iterable can't be randomized!");
        }

        List<T> list = toList(iterable);
        Collections.shuffle(list);
        return list.get(0);
    }

    private IterableUtils() {
    }
}
