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

package com.graphaware.propertycontainer.dto.string.relationship;

import com.graphaware.propertycontainer.dto.common.relationship.HasType;
import com.graphaware.propertycontainer.dto.string.BaseStringConverter;
import com.graphaware.propertycontainer.dto.string.StringConverter;
import com.graphaware.propertycontainer.dto.string.property.StringPropertiesConverter;

import java.util.Map;

/**
 * Base-class for {@link com.graphaware.propertycontainer.dto.string.StringConverter}s for {@link org.neo4j.graphdb.Relationship} representations.
 */
public abstract class BaseRelationshipStringConverter<T extends HasType> extends BaseStringConverter<T> {

    private static final StringConverter<Map<String, String>> PROPERTIES_CONVERTER = new StringPropertiesConverter();

    /**
     * Split a string representing a relationship into the relationship type and additional information (direction,
     * properties, or both).
     *
     * @param string      to split.
     * @param prefix      that the String has (if any), which should be removed before the conversion, null or empty for no prefix.
     * @param separator   of information.
     * @param min         the minimum number of elements the string must be split into.
     * @param max         the maximum number of elements the string must be split into.
     * @param mustContain message for exception indicating the minimum information the string must contain, in case it
     *                    could not be split into at least min elements.
     * @return split string.
     * @throws IllegalArgumentException in case the couldn't be reached or if the first element (which always indicates
     *                                  {@link org.neo4j.graphdb.RelationshipType}) is an empty String.
     */
    protected String[] split(String string, String prefix, String separator, int min, int max, String mustContain) {
        String[] split = stripPrefix(string, prefix).split(separator, max);
        if (split.length < min) {
            throw new IllegalArgumentException("Relationship Representation must contain at least " + mustContain + "!");
        }

        if (split[0].trim().isEmpty()) {
            throw new IllegalArgumentException("Relationship type must not be empty!");
        }
        return split;
    }

    /**
     * Convert properties from string to map of key-value pairs.
     *
     * @param string    to convert.
     * @param separator of information.
     * @return properties as key-value Strings map.
     */
    protected Map<String, String> convertProperties(String string, String separator) {
        return PROPERTIES_CONVERTER.fromString(string, "", separator);
    }
}
