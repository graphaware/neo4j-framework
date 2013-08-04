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

import com.graphaware.propertycontainer.dto.common.property.ImmutableProperties;
import com.graphaware.propertycontainer.dto.common.propertycontainer.HasProperties;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.propertycontainer.util.PropertyContainerUtils.propertiesToObjectMap;

/**
 * Abstract base-class for {@link ImmutableRelationship} implementations.
 *
 * @param <V> type with which property values are represented.
 * @param <P> type of properties held by this relationship representation.
 */
public abstract class BaseRelationship<V, P extends ImmutableProperties<V>> extends BaseHasType {

    private final P properties;

    /**
     * Construct a relationship representation.
     *
     * @param relationship Neo4j relationship to represent.
     */
    protected BaseRelationship(Relationship relationship) {
        super(relationship);
        this.properties = newProperties(propertiesToObjectMap(relationship));
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead.
     *
     * @param relationship Neo4j relationship to represent.
     * @param properties   to use as if they were in the relationship.
     */
    protected BaseRelationship(Relationship relationship, P properties) {
        super(relationship);
        this.properties = properties;
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead.
     *
     * @param relationship Neo4j relationship to represent.
     * @param properties   to use as if they were in the relationship.
     */
    protected BaseRelationship(Relationship relationship, Map<String, ?> properties) {
        super(relationship);
        this.properties = newProperties(properties);
    }

    /**
     * Construct a relationship representation with no properties.
     *
     * @param type type.
     */
    protected BaseRelationship(RelationshipType type) {
        super(type);
        this.properties = newProperties(Collections.<String, Object>emptyMap());
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param properties props.
     */
    protected BaseRelationship(RelationshipType type, P properties) {
        super(type);
        this.properties = properties;
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param properties props.
     */
    protected BaseRelationship(RelationshipType type, Map<String, ?> properties) {
        super(type);
        this.properties = newProperties(properties);
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    protected BaseRelationship(HasTypeAndProperties<?, ?> relationship) {
        super(relationship);
        this.properties = newProperties(relationship.getProperties().getProperties());
    }

    /**
     * Create properties representation for this relationship a map of properties.
     *
     * @param properties to create a representation from.
     * @return created properties.
     */
    protected abstract P newProperties(Map<String, ?> properties);

    /**
     * Get this relationship's properties.
     *
     * @return properties.
     */
    public P getProperties() {
        return properties;
    }

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} and properties of this relationship representation equal to the type of the
     * given {@link org.neo4j.graphdb.Relationship}?
     *
     * @param relationship to compare.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} and properties of this relationship representation matches the type
     *         of the given {@link org.neo4j.graphdb.Relationship}.
     */
    public boolean matches(Relationship relationship) {
        return super.matches(relationship) && properties.matches(relationship);
    }

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance equal to the
     * type and properties of the given {@link HasTypeAndProperties}?
     *
     * @param hasTypeAndProperties to check match.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance
     *         equal to the type and properties of the given {@link HasTypeAndProperties}.
     */
    public boolean matches(HasTypeAndProperties<V, ?> hasTypeAndProperties) {
        return super.matches(hasTypeAndProperties) && matches((HasProperties<V, ?>) hasTypeAndProperties);
    }

    /**
     * Do the properties of this instance match (are they the same as) the properties held by the given
     * {@link org.neo4j.graphdb.PropertyContainer}?
     *
     * @param propertyContainer to check.
     * @return true iff the properties held by this instance match the properties held by the given
     *         {@link org.neo4j.graphdb.PropertyContainer}.
     */
    public boolean matches(PropertyContainer propertyContainer) {
        return propertyContainer instanceof Relationship && matches((Relationship) propertyContainer);
    }

    /**
     * Do the properties of this instance match (are they the same as) the properties held by the given
     * {@link HasProperties}?
     *
     * @param hasProperties to check.
     * @return true iff the properties held by this instance match the properties held by the given
     *         {@link HasProperties}.
     */
    public boolean matches(HasProperties<V, ?> hasProperties) {
        return properties.matches(hasProperties.getProperties());
    }

    /**
     * Get the relationship direction.
     *
     * @return direction.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseRelationship that = (BaseRelationship) o;

        //noinspection RedundantIfStatement
        if (!properties.equals(that.properties)) return false;

        return true;
    }

    /**
     * Get the relationship direction.
     *
     * @return direction.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + properties.hashCode();
        return result;
    }
}
