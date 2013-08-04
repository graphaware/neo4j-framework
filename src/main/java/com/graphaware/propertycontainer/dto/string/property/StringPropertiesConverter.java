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

package com.graphaware.propertycontainer.dto.string.property;

import com.graphaware.propertycontainer.dto.common.BaseStringConverter;
import com.graphaware.propertycontainer.dto.common.StringConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link StringConverter} for property representations with {@link String} values.
 */
public class StringPropertiesConverter extends BaseStringConverter<Map<String, String>> implements StringConverter<Map<String, String>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> fromString(String string, String prefix, String separator) {
        checkCorrectPrefix(string, prefix);
        checkCorrectSeparator(separator);

        Map<String, String> result = new HashMap<>();

        String withoutPrefix = stripPrefix(string, prefix);

        if (!withoutPrefix.trim().isEmpty()) {
            String[] tokens = withoutPrefix.split(separator);
            for (int i = 0; i < tokens.length; i += 2) {
                String key = tokens[i];
                String value = "";
                if (i < tokens.length - 1) {
                    value = tokens[i + 1];
                }

                result.put(key, value);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(Map<String, String> object, String prefix, String separator) {
        checkCorrectSeparator(separator);

        if (object.isEmpty()) {
            return prefix(prefix);
        }

        StringBuilder stringBuilder = new StringBuilder(prefix(prefix));
        for (String key : object.keySet()) {
            stringBuilder
                    .append(key)
                    .append(separator)
                    .append(object.get(key))
                    .append(separator);
        }

        return stringBuilder.deleteCharAt(stringBuilder.length() - separator.length()).toString();
    }
}
