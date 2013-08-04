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

package com.graphaware.propertycontainer.dto.common;

/**
 * A component capable of converting and object to and from {@link String}.
 *
 * @param <T> type of the object this component is capable of converting.
 */
public interface StringConverter<T> {

    /**
     * Create an object from String.
     *
     * @param string    to create the object from.
     * @param separator of different pieces of information in the string, ideally a single character, must not be null or empty.
     * @return the object.
     */
    T fromString(String string, String separator);

    /**
     * Create an object from String.
     *
     * @param string    to create the object from.
     * @param prefix    that the String has (if any), which should be removed before the conversion, null or empty for no prefix.
     * @param separator of different pieces of information in the string, ideally a single character, must not be null or empty.
     * @return the object.
     */
    T fromString(String string, String prefix, String separator);

    /**
     * Convert an object to String.
     *
     * @param object    to be converted.
     * @param separator of different pieces of information in the string, ideally a single character, must not be null or empty.
     * @return String representation of the object.
     */
    String toString(T object, String separator);

    /**
     * Convert an object to String.
     *
     * @param object    to be converted.
     * @param prefix    that the String should get (if any) after the conversion, null or empty for no prefix.
     * @param separator of different pieces of information in the string, ideally a single character, must not be null or empty.
     * @return String representation of the object.
     */
    String toString(T object, String prefix, String separator);
}
