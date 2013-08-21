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
 * Base-class for {@link StringConverter}s for {@link org.neo4j.graphdb.Relationship} representations.
 *
 * @param <T> type of the object this component is capable of converting.
 */
public abstract class BaseStringConverter<T> {

    /**
     * Create an object from String.
     *
     * @param string    to create the object from.
     * @param separator of different pieces of information in the string.
     * @return the object.
     */
    public T fromString(String string, String separator) {
        return fromString(string, "", separator);
    }

    /**
     * Create an object from String.
     *
     * @param string    to create the object from.
     * @param prefix    that the String has (if any), which should be removed before the conversion, null or empty for no prefix.
     * @param separator of different pieces of information in the string.
     * @return the object.
     */
    protected abstract T fromString(String string, String prefix, String separator);

    /**
     * Convert an object to String.
     *
     * @param object    to be converted.
     * @param separator of different pieces of information in the string.
     * @return String representation of the object.
     */
    public String toString(T object, String separator) {
        return toString(object, "", separator);
    }

    /**
     * Convert an object to String.
     *
     * @param object    to be converted.
     * @param prefix    that the String should get (if any) after the conversion, null or empty for no prefix.
     * @param separator of different pieces of information in the string.
     * @return String representation of the object.
     */
    protected abstract String toString(T object, String prefix, String separator);

    /**
     * Check that a string has the correct prefix.
     *
     * @param string to check.
     * @param prefix that the string must have. Null or empty for none.
     * @throws IllegalArgumentException if the check failed.
     */
    protected void checkCorrectPrefix(String string, String prefix) {
        if (prefix != null && !prefix.isEmpty() && !string.startsWith(prefix)) {
            throw new IllegalArgumentException("Not a desired relationship representation! Must start with " + prefix);
        }
    }

    /**
     * Strip prefix from a string.
     *
     * @param string to strip prefix from.
     * @param prefix to strip, null or empty for none.
     * @return string with stripped prefix.
     */
    protected String stripPrefix(String string, String prefix) {
        int prefixLength = prefix == null ? 0 : prefix.length();
        return string.substring(prefixLength);
    }

    /**
     * Check that a separator is correct, i.e. not null or empty.
     *
     * @param separator to check.
     * @throws IllegalArgumentException if the check failed.
     */
    protected void checkCorrectSeparator(String separator) {
        if (separator == null || "".equals(separator.trim())) {
            throw new IllegalArgumentException("Separator must not be null or empty!");
        }
    }

    /**
     * Create prefix from desired prefix.
     *
     * @param prefix desired, can be null.
     * @return prefix, if the parameter is null, then empty string.
     */
    protected String prefix(String prefix) {
        if (prefix == null) {
            return "";
        }
        return prefix;
    }
}
