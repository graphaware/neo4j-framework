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

package com.graphaware.propertycontainer.dto.plain.relationship;

import com.graphaware.propertycontainer.dto.common.property.ImmutableProperties;
import com.graphaware.propertycontainer.dto.common.relationship.BaseRelationship;
import com.graphaware.propertycontainer.dto.common.relationship.ImmutableRelationship;
import com.graphaware.propertycontainer.dto.plain.property.ImmutablePropertiesImpl;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

/**
 * Simple implementation of {@link com.graphaware.propertycontainer.dto.common.relationship.ImmutableRelationship},
 * holding {@link ImmutableProperties} with {@link Object} values.
 */
public class ImmutableRelationshipImpl extends BaseRelationship<Object, ImmutableProperties<Object>> implements ImmutableRelationship<Object, ImmutableProperties<Object>> {

    /**
     * Construct a relationship representation.
     *
     * @param relationship Neo4j relationship to represent.
     */
    public ImmutableRelationshipImpl(Relationship relationship) {
        super(relationship);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead.
     *
     * @param relationship Neo4j relationship to represent.
     * @param properties   to use as if they were in the relationship.
     */
    public ImmutableRelationshipImpl(Relationship relationship, ImmutableProperties<Object> properties) {
        super(relationship, properties);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead.
     *
     * @param relationship Neo4j relationship to represent.
     * @param properties   to use as if they were in the relationship.
     */
    public ImmutableRelationshipImpl(Relationship relationship, Map<String, Object> properties) {
        super(relationship, properties);
    }

    /**
     * Construct a relationship representation with no properties.
     *
     * @param type type.
     */
    public ImmutableRelationshipImpl(RelationshipType type) {
        super(type);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param properties props.
     */
    public ImmutableRelationshipImpl(RelationshipType type, ImmutableProperties<Object> properties) {
        super(type, properties);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param properties props.
     */
    public ImmutableRelationshipImpl(RelationshipType type, Map<String, Object> properties) {
        super(type, properties);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    public ImmutableRelationshipImpl(ImmutableRelationship<Object, ImmutableProperties<Object>> relationship) {
        super(relationship);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ImmutableProperties<Object> newProperties(Map<String, ?> properties) {
        return new ImmutablePropertiesImpl(properties);
    }
}
