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

import static org.neo4j.graphdb.Direction.valueOf;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * {@link StringConverter} for {@link SerializableDirectedRelationship}s.
 */
public class DirectedRelationshipStringConverter extends BaseRelationshipStringConverter<SerializableDirectedRelationship<? extends SerializableProperties>> implements StringConverter<SerializableDirectedRelationship<? extends SerializableProperties>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializableDirectedRelationship<? extends SerializableProperties> fromString(String string, String prefix, String separator) {
        checkCorrectPrefix(string, prefix);
        checkCorrectSeparator(separator);

        String[] split = split(string, prefix, separator, 2, 3, "type and direction");

        return new SerializableDirectedRelationshipImpl(withName(split[0]), valueOf(split[1]), convertProperties(split.length > 2 ? split[2] : "", separator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(SerializableDirectedRelationship<? extends SerializableProperties> object, String prefix, String separator) {
        checkCorrectSeparator(separator);

        String result = prefix(prefix) + object.getType().name() + separator + object.getDirection().toString();
        String props = object.getProperties().toString(separator);
        if (props.length() > 0) {
            result = result + separator + props;
        }

        return result;
    }
}
