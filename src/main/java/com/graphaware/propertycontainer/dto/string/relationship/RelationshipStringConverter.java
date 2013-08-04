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

import com.graphaware.propertycontainer.dto.common.StringConverter;
import com.graphaware.propertycontainer.dto.common.relationship.BaseRelationshipStringConverter;
import com.graphaware.propertycontainer.dto.string.property.SerializableProperties;

import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * {@link StringConverter} for {@link SerializableRelationship}.
 */
public class RelationshipStringConverter extends BaseRelationshipStringConverter<SerializableRelationship<? extends SerializableProperties>> implements StringConverter<SerializableRelationship<? extends SerializableProperties>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializableRelationship<? extends SerializableProperties> fromString(String string, String prefix, String separator) {
        checkCorrectPrefix(string, prefix);
        checkCorrectSeparator(separator);

        String[] split = split(string, prefix, separator, 1, 2, "type");

        return new SerializableRelationshipImpl(withName(split[0]), convertProperties(split.length > 1 ? split[1] : "", separator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(SerializableRelationship<? extends SerializableProperties> object, String prefix, String separator) {
        checkCorrectSeparator(separator);

        String result = prefix(prefix) + object.getType().name();
        String props = object.getProperties().toString(separator);
        if (props.length() > 0) {
            result = result + separator + props;
        }

        return result;
    }
}
