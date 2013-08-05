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
import com.graphaware.propertycontainer.dto.common.relationship.BaseDirectedRelationship;
import com.graphaware.propertycontainer.dto.common.relationship.HasTypeDirectionAndProperties;
import com.graphaware.propertycontainer.dto.string.property.SerializableProperties;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

/**
 * Base-class for {@link SerializableDirectedRelationship} implementations.
 *
 * @param <P> type of properties held by this relationship representation.
 */
public abstract class BaseSerializableDirectedRelationship<P extends SerializableProperties> extends BaseDirectedRelationship<String, P> implements SerializableDirectedRelationship<P> {

    private static final StringConverter<SerializableDirectedRelationship<? extends SerializableProperties>> STRING_CONVERTER = new DirectedRelationshipStringConverter();

    /**
     * Construct a relationship representation. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     */
    protected BaseSerializableDirectedRelationship(Relationship relationship, Node pointOfView) {
        super(relationship, pointOfView);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     * @param properties   to use as if they were in the relationship.
     */
    protected BaseSerializableDirectedRelationship(Relationship relationship, Node pointOfView, P properties) {
        super(relationship, pointOfView, properties);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     * @param properties   to use as if they were in the relationship.
     */
    protected BaseSerializableDirectedRelationship(Relationship relationship, Node pointOfView, Map<String, ?> properties) {
        super(relationship, pointOfView, properties);
    }

    /**
     * Construct a relationship representation with no properties.
     *
     * @param type      type.
     * @param direction direction.
     */
    protected BaseSerializableDirectedRelationship(RelationshipType type, Direction direction) {
        super(type, direction);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param direction  direction.
     * @param properties props.
     */
    protected BaseSerializableDirectedRelationship(RelationshipType type, Direction direction, P properties) {
        super(type, direction, properties);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param direction  direction.
     * @param properties props.
     */
    protected BaseSerializableDirectedRelationship(RelationshipType type, Direction direction, Map<String, ?> properties) {
        super(type, direction, properties);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    protected BaseSerializableDirectedRelationship(HasTypeDirectionAndProperties<String, ?> relationship) {
        super(relationship);
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    string to construct relationship from. Must be of the form type#direction#key1#value1#key2#value2...
     *                  (assuming # separator)
     * @param separator of information, ideally a single character, must not be null or empty.
     */
    protected BaseSerializableDirectedRelationship(String string, String separator) {
        this(STRING_CONVERTER.fromString(string, separator));
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    string to construct relationship from. Must be of the form prefix + type#direction#key1#value1#key2#value2...
     *                  (assuming # separator)
     * @param prefix    of the string that should be removed before conversion.
     * @param separator of information, ideally a single character, must not be null or empty.
     */
    protected BaseSerializableDirectedRelationship(String string, String prefix, String separator) {
        this(STRING_CONVERTER.fromString(string, prefix, separator));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(String separator) {
        return STRING_CONVERTER.toString(this, separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(String prefix, String separator) {
        return STRING_CONVERTER.toString(this, prefix, separator);
    }

    /**
     * Should only be used for logging and other miscellaneous tasks, not for storing the relationship!
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getType().name() + "#" + getDirection().toString() + "#" + getProperties().toString();
    }
}
