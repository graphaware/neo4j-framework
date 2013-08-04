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
import com.graphaware.propertycontainer.util.DirectionUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

import static com.graphaware.propertycontainer.util.DirectionUtils.resolveDirection;

/**
 * Abstract base-class for {@link ImmutableDirectedRelationship} implementations.
 *
 * @param <V> type with which property values are represented.
 * @param <P> type of properties held by this relationship representation.
 */
public abstract class BaseDirectedRelationship<V, P extends ImmutableProperties<V>> extends BaseRelationship<V, P> {

    private final Direction direction;

    /**
     * Construct a relationship representation. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     */
    protected BaseDirectedRelationship(Relationship relationship, Node pointOfView) {
        super(relationship);
        this.direction = resolveDirection(relationship, pointOfView, Direction.BOTH);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead. If the start node of this relationship is the
     * same as the end node, the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     * @param properties   to use as if they were in the relationship.
     */
    protected BaseDirectedRelationship(Relationship relationship, Node pointOfView, P properties) {
        super(relationship, properties);
        this.direction = resolveDirection(relationship, pointOfView, Direction.BOTH);
    }

    /**
     * Construct a relationship representation. Please note that using this constructor, the actual properties on the
     * relationship are ignored! The provided properties are used instead. If the start node of this relationship is the
     * same as the end node, the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     * @param properties   to use as if they were in the relationship.
     */
    protected BaseDirectedRelationship(Relationship relationship, Node pointOfView, Map<String, ?> properties) {
        super(relationship, properties);
        this.direction = resolveDirection(relationship, pointOfView, Direction.BOTH);
    }

    /**
     * Construct a relationship representation with no properties.
     *
     * @param type      type.
     * @param direction direction.
     */
    protected BaseDirectedRelationship(RelationshipType type, Direction direction) {
        super(type);
        this.direction = direction;
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param direction  direction.
     * @param properties props.
     */
    protected BaseDirectedRelationship(RelationshipType type, Direction direction, P properties) {
        super(type, properties);
        this.direction = direction;
    }

    /**
     * Construct a relationship representation.
     *
     * @param type       type.
     * @param direction  direction.
     * @param properties props.
     */
    protected BaseDirectedRelationship(RelationshipType type, Direction direction, Map<String, ?> properties) {
        super(type, properties);
        this.direction = direction;
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    protected BaseDirectedRelationship(HasTypeDirectionAndProperties<V, ?> relationship) {
        super(relationship);
        this.direction = relationship.getDirection();
    }

    /**
     * Get the relationship direction.
     *
     * @return direction.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, use {@link #matches(org.neo4j.graphdb.Relationship, org.neo4j.graphdb.Node)} instead.
     */
    @Override
    public final boolean matches(Relationship relationship) {
        throw new UnsupportedOperationException("Please use the matches(Relationship, Node) method on directed relationship representations");
    }

    /**
     * Do the {@link org.neo4j.graphdb.Direction}, {@link org.neo4j.graphdb.RelationshipType}, and properties of this component equal to the
     * direction, type, and properties of the given {@link org.neo4j.graphdb.Relationship}? Direction is taken from a
     * given {@link org.neo4j.graphdb.Node}'s point of view.
     *
     * @param relationship to compare.
     * @param pointOfView  {@link org.neo4j.graphdb.Node} whose point of view the {@link org.neo4j.graphdb.Relationship} {@link org.neo4j.graphdb.Direction} is being determined.
     * @return true iff the {@link org.neo4j.graphdb.Direction} and {@link org.neo4j.graphdb.RelationshipType} of this component matches
     *         the {@link org.neo4j.graphdb.Direction} and {@link org.neo4j.graphdb.RelationshipType} of the given {@link org.neo4j.graphdb.Relationship}.
     */
    public boolean matches(Relationship relationship, Node pointOfView) {
        return super.matches(relationship) && DirectionUtils.matches(relationship, pointOfView, getDirection());
    }

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} and {@link org.neo4j.graphdb.Direction} of this instance match the type and
     * direction of the given {@link HasTypeAndDirection}? Matching types must have equal names; matching directions must be
     * equal, or at least one of them must be {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param hasTypeAndDirection to check match.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} and {@link org.neo4j.graphdb.Direction}  of this instance match
     *         type and direction of the given {@link HasTypeAndDirection}.
     */
    public boolean matches(HasTypeAndDirection hasTypeAndDirection) {
        return super.matches(hasTypeAndDirection) && DirectionUtils.matches(direction, hasTypeAndDirection.getDirection());
    }

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance equal to the
     * type and properties of the given {@link HasTypeDirectionAndProperties}?
     *
     * @param hasTypeDirectionAndProperties to check match.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance
     *         equal to the type and properties of the given {@link HasTypeDirectionAndProperties}.
     */
    public boolean matches(HasTypeDirectionAndProperties<V, ?> hasTypeDirectionAndProperties) {
        return matches((HasTypeAndDirection) hasTypeDirectionAndProperties) && super.matches(hasTypeDirectionAndProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseDirectedRelationship that = (BaseDirectedRelationship) o;

        //noinspection RedundantIfStatement
        if (direction != that.direction) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }
}
