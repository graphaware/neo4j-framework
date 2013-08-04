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

import com.graphaware.propertycontainer.dto.common.relationship.HasTypeDirectionAndProperties;
import com.graphaware.propertycontainer.dto.string.property.CopyMakingSerializableProperties;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

/**
 * An abstract base-class for {@link CopyMakingSerializableDirectedRelationship} implementations.
 *
 * @param <P> type of properties held by this relationship representation.
 * @param <R> type of relationship returned with and without property.
 */
public abstract class BaseCopyMakingSerializableDirectedRelationship<P extends CopyMakingSerializableProperties<P>, R extends CopyMakingSerializableDirectedRelationship<P, R>> extends BaseSerializableDirectedRelationship<P> {

    /**
     * Construct a relationship representation. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     */
    protected BaseCopyMakingSerializableDirectedRelationship(Relationship relationship, Node pointOfView) {
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
    protected BaseCopyMakingSerializableDirectedRelationship(Relationship relationship, Node pointOfView, P properties) {
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
    protected BaseCopyMakingSerializableDirectedRelationship(Relationship relationship, Node pointOfView, Map<String, ?> properties) {
        super(relationship, pointOfView, properties);
    }

    /**
     * Construct a relationship representation with no properties.
     *
     * @param type      type.
     * @param direction direction.
     */
    protected BaseCopyMakingSerializableDirectedRelationship(RelationshipType type, Direction direction) {
        super(type, direction);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param direction  direction.
     * @param properties props.
     */
    protected BaseCopyMakingSerializableDirectedRelationship(RelationshipType type, Direction direction, P properties) {
        super(type, direction, properties);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param direction  direction.
     * @param properties props.
     */
    protected BaseCopyMakingSerializableDirectedRelationship(RelationshipType type, Direction direction, Map<String, ?> properties) {
        super(type, direction, properties);
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    string to construct relationship from. Must be of the form type#direction#key1#value1#key2#value2...
     *                  (assuming # separator)
     * @param separator of information, ideally a single character, must not be null or empty.
     */
    protected BaseCopyMakingSerializableDirectedRelationship(String string, String separator) {
        super(string, separator);
    }

    /**
     * Construct a relationship representation from a string.
     *
     * @param string    string to construct relationship from. Must be of the form prefix + type#direction#key1#value1#key2#value2...
     *                  (assuming # separator)
     * @param prefix    of the string that should be removed before conversion.
     * @param separator of information, ideally a single character, must not be null or empty.
     */
    protected BaseCopyMakingSerializableDirectedRelationship(String string, String prefix, String separator) {
        super(string, prefix, separator);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    protected BaseCopyMakingSerializableDirectedRelationship(HasTypeDirectionAndProperties<String, ?> relationship) {
        super(relationship);
    }

    /**
     * Make a copy of this instance with an extra key-value pair.
     *
     * @param key   new key
     * @param value new value
     * @return copy of this instance with an extra key-value pair.
     */
    public R with(String key, Object value) {
        return newRelationship(getType(), getDirection(), getProperties().with(key, value).getProperties());
    }

    /**
     * Make a copy of this instance with a single key-value pair missing.
     *
     * @param key to omit in the copy. If the key does not exist in the representation, an identical copy will be returned.
     * @return copy of this instance except one key-value pair.
     */
    public R without(String key) {
        return newRelationship(getType(), getDirection(), getProperties().without(key).getProperties());
    }

    /**
     * Create a new instance of this relationship representation.
     *
     * @param type       type.
     * @param direction  direction.
     * @param properties props.
     * @return new instance.
     */
    protected abstract R newRelationship(RelationshipType type, Direction direction, Map<String, ?> properties);
}
