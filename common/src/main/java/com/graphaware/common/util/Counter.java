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

import java.util.HashMap;
import java.util.Map;

/**
 * Simple counter of objects of type <T>. It's not thread-safe and should not be shared between threads.
 */
public class Counter<T> {

    private final Map<T, Long> counts = new HashMap<>();

    public void increment(T object) {
        increment(object, 1);
    }

    public void increment(T object, long delta) {
        if (!counts.containsKey(object)) {
            counts.put(object, 0L);
        }

        counts.put(object, counts.get(object) + delta);
    }

    public void decrement(T object) {
        decrement(object, 1);
    }

    public void decrement(T object, long delta) {
        increment(object, -delta);
    }

    public void reset() {
        counts.clear();
    }

    public long getCount(T object) {
        increment(object, 0L);
        return counts.get(object);
    }
}
