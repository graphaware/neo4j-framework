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

import com.graphaware.propertycontainer.dto.common.relationship.HasTypeAndProperties;
import com.graphaware.propertycontainer.dto.string.property.SerializableProperties;
import com.graphaware.propertycontainer.dto.string.property.SerializablePropertiesImpl;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

/**
 * Simple implementation of {@link SerializableRelationship}.
 */
public class SerializableRelationshipImpl extends BaseSerializableRelationship<SerializableProperties> implements SerializableRelationship<SerializableProperties> {

    /**
     * Construct a relationship representation.
     *
     * @param relationship Neo4j relationship to represent.
     */
    public SerializableRelationshipImpl(Relationship relationship) {
        super(relationship);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead.
     *
     * @param relationship Neo4j relationship to represent.
     * @param properties   to use as if they were in the relationship.
     */
    public SerializableRelationshipImpl(Relationship relationship, SerializableProperties properties) {
        super(relationship, properties);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead.
     *
     * @param relationship Neo4j relationship to represent.
     * @param properties   to use as if they were in the relationship.
     */
    public SerializableRelationshipImpl(Relationship relationship, Map<String, String> properties) {
        super(relationship, properties);
    }

    /**
     * Construct a relationship representation with no properties.
     *
     * @param type type.
     */
    public SerializableRelationshipImpl(RelationshipType type) {
        super(type);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param properties props.
     */
    public SerializableRelationshipImpl(RelationshipType type, SerializableProperties properties) {
        super(type, properties);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param properties props.
     */
    public SerializableRelationshipImpl(RelationshipType type, Map<String, String> properties) {
        super(type, properties);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    public SerializableRelationshipImpl(HasTypeAndProperties<String, ?> relationship) {
        super(relationship);
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    string to construct relationship from. Must be of the form type#key1#value1#key2#value2...
     *                  (assuming # separator)
     * @param separator of information, ideally a single character, must not be null or empty.
     */
    public SerializableRelationshipImpl(String string, String separator) {
        super(string, separator);
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    string to construct relationship from. Must be of the form prefix + type#key1#value1#key2#value2...
     *                  (assuming # separator)
     * @param prefix    of the string that should be removed before conversion.
     * @param separator of information, ideally a single character, must not be null or empty.
     */
    public SerializableRelationshipImpl(String string, String prefix, String separator) {
        super(string, prefix, separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SerializableProperties newProperties(Map<String, ?> properties) {
        return new SerializablePropertiesImpl(properties);
    }
}
