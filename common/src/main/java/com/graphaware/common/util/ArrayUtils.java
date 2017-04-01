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

import java.util.Arrays;
import java.util.Map;

/**
 * Static utility methods for dealing with arrays.
 */
public final class ArrayUtils {

    /**
     * Check if the given object is a primitive array.
     *
     * @param o to check.
     * @return true iff o is a primitive array.
     */
    public static boolean isPrimitiveArray(Object o) {
        if (o instanceof byte[]) {
            return true;
        } else if (o instanceof char[]) {
            return true;
        } else if (o instanceof boolean[]) {
            return true;
        } else if (o instanceof long[]) {
            return true;
        } else if (o instanceof double[]) {
            return true;
        } else if (o instanceof int[]) {
            return true;
        } else if (o instanceof short[]) {
            return true;
        } else if (o instanceof float[]) {
            return true;
        }
        return false;
    }

    /**
     * Check if the given object is a primitive array or an array of Strings.
     *
     * @param o to check.
     * @return true iff o is a primitive array or an array of Strings.
     */
    public static boolean isPrimitiveOrStringArray(Object o) {
        if (isPrimitiveArray(o)) {
            return true;
        } else if (o instanceof String[]) {
            return true;
        }
        return false;
    }

    /**
     * Check if the given object is an array.
     *
     * @param o to check.
     * @return true iff o is an array.
     */
    public static boolean isArray(Object o) {
        if (isPrimitiveArray(o)) {
            return true;
        } else if (o instanceof Object[]) {
            return true;
        }
        return false;
    }

    /**
     * Check whether two objects, potentially arrays, are equal.
     *
     * @param o1 object 1
     * @param o2 object 2
     * @return true iff o1 and o2 are equal.
     */
    public static boolean arrayFriendlyEquals(Object o1, Object o2) {
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals(((byte[]) o1), (byte[]) o2);
        } else if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals(((char[]) o1), (char[]) o2);
        } else if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals(((boolean[]) o1), (boolean[]) o2);
        } else if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals(((long[]) o1), (long[]) o2);
        } else if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals(((double[]) o1), (double[]) o2);
        } else if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals(((int[]) o1), (int[]) o2);
        } else if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals(((short[]) o1), (short[]) o2);
        } else if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals(((float[]) o1), (float[]) o2);
        } else if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals(((Object[]) o1), (Object[]) o2);
        } else return o1.equals(o2);
    }

    /**
     * Get has code of the object, which could be an arrays. Hash codes of arrays that are equal according
     * to {@link #arrayFriendlyEquals(Object, Object)} are the same.
     *
     * @param o to get a hash code for.
     * @return hash code.
     */
    public static int arrayFriendlyHashCode(Object o) {
        if (o instanceof byte[]) {
            return Arrays.hashCode((byte[]) o);
        } else if (o instanceof char[]) {
            return Arrays.hashCode((char[]) o);
        } else if (o instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) o);
        } else if (o instanceof long[]) {
            return Arrays.hashCode((long[]) o);
        } else if (o instanceof double[]) {
            return Arrays.hashCode((double[]) o);
        } else if (o instanceof int[]) {
            return Arrays.hashCode((int[]) o);
        } else if (o instanceof short[]) {
            return Arrays.hashCode((short[]) o);
        } else if (o instanceof float[]) {
            return Arrays.hashCode((float[]) o);
        } else if (o instanceof Object[]) {
            return Arrays.hashCode((Object[]) o);
        }
        return o.hashCode();
    }

    /**
     * Convert a primitive array or an array of Strings to string.
     *
     * @param o to convert.
     * @return as string.
     * @throws IllegalArgumentException if o isn't a primitive array or an array of Strings.
     */
    public static String primitiveOrStringArrayToString(Object o) {
        if (o instanceof byte[]) {
            return Arrays.toString((byte[]) o);
        } else if (o instanceof char[]) {
            return Arrays.toString((char[]) o);
        } else if (o instanceof boolean[]) {
            return Arrays.toString((boolean[]) o);
        } else if (o instanceof long[]) {
            return Arrays.toString((long[]) o);
        } else if (o instanceof double[]) {
            return Arrays.toString((double[]) o);
        } else if (o instanceof int[]) {
            return Arrays.toString((int[]) o);
        } else if (o instanceof short[]) {
            return Arrays.toString((short[]) o);
        } else if (o instanceof float[]) {
            return Arrays.toString((float[]) o);
        } else if (o instanceof String[]) {
            return Arrays.toString((String[]) o);
        }
        throw new IllegalArgumentException("Object must be a primitive array!");
    }

    /**
     * Check equality of both maps that can have arrays as values.
     *
     * @param map1
     * @param map2
     * @return true iff the maps are equal.
     */
    public static <T> boolean arrayFriendlyMapEquals(Map<String, T> map1, Map<String, T> map2) {
        if (map1.size() != map2.size())
            return false;

        try {
            for (Map.Entry<String, T> e : map1.entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    if (!(map2.get(key) == null && map2.containsKey(key)))
                        return false;
                } else {
                    if (!arrayFriendlyEquals(value, map2.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    private ArrayUtils() {
    }
}
