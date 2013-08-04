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

package com.graphaware.propertycontainer.dto.common.relationship;

import com.graphaware.propertycontainer.dto.common.StringConverter;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * {@link TypeAndDirection} convertible to and from {@link String}. Could be used as string-convertible
 * {@link org.neo4j.graphdb.Relationship} representation when not interested in properties.
 */
public class SerializableTypeAndDirectionImpl extends TypeAndDirection implements SerializableTypeAndDirection {

    private static final StringConverter<HasTypeAndDirection> STRING_CONVERTER = new TypeAndDirectionStringConverter();

    /**
     * Construct a relationship representation. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     */
    public SerializableTypeAndDirectionImpl(Relationship relationship, Node pointOfView) {
        super(relationship, pointOfView);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type      type.
     * @param direction direction.
     */
    public SerializableTypeAndDirectionImpl(RelationshipType type, Direction direction) {
        super(type, direction);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    public SerializableTypeAndDirectionImpl(HasTypeAndDirection relationship) {
        super(relationship);
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    to construct properties from. Must be of the form type#direction (assuming # separator).
     * @param separator of information.
     */
    public SerializableTypeAndDirectionImpl(String string, String separator) {
        this(STRING_CONVERTER.fromString(string, separator));
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    to construct properties from. Must be of the form type#direction (assuming # separator).
     * @param prefix    of the string that should be removed before conversion.
     * @param separator of information.
     */
    public SerializableTypeAndDirectionImpl(String string, String prefix, String separator) {
        this(STRING_CONVERTER.fromString(string, prefix, separator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(String prefix, String separator) {
        return STRING_CONVERTER.toString(this, prefix, separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(String separator) {
        return STRING_CONVERTER.toString(this, separator);
    }
}
