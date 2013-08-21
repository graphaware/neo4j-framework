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

package com.graphaware.propertycontainer.dto.string;

/**
 * Object that can be converted to/from String. Implementations should provide corresponding {@link String}-based constructors, so that:
 * <code>
 * //no prefix
 * String s = "one#two";
 * SerializableImpl o = new SerializableImpl(s, "#");
 * assert s.equals(o.toString("#"));
 * </code>
 * and
 * <code>
 * String s = "_prefix_one#two";
 * SerializableImpl o = new SerializableImpl(s, "_prefix_", "#");
 * assert s.equals(o.toString("_prefix_", "#"));
 * </code>
 */
public interface Serializable {

    /**
     * Convert this object to a String representation.
     *
     * @param separator of information, ideally a single character, must not be null or empty.
     * @return String of the form something#somethingelse (assuming # separator).
     */
    String toString(String separator);

    /**
     * Convert this object to a String representation.
     *
     * @param prefix    of the String representation, null or empty for none.
     * @param separator of information, ideally a single character, must not be null or empty.
     * @return String of the form prefix + something#somethingelse (assuming # separator).
     */
    String toString(String prefix, String separator);
}
