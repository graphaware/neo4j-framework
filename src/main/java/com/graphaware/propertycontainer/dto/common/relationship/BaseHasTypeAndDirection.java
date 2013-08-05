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

import com.graphaware.propertycontainer.util.DirectionUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import static com.graphaware.propertycontainer.util.DirectionUtils.resolveDirection;


/**
 * Base-class for {@link HasTypeAndDirection} implementations.
 */
public abstract class BaseHasTypeAndDirection extends BaseHasType {

    private final Direction direction;

    /**
     * Construct a relationship representation. If the start node of this relationship is the same as the end node,
     * the direction will be resolved as {@link org.neo4j.graphdb.Direction#BOTH}.
     *
     * @param relationship Neo4j relationship to represent.
     * @param pointOfView  node which is looking at this relationship and thus determines its direction.
     */
    protected BaseHasTypeAndDirection(Relationship relationship, Node pointOfView) {
        super(relationship);
        this.direction = resolveDirection(relationship, pointOfView, Direction.BOTH);
    }

    /**
     * Construct a relationship representation.
     *
     * @param type      type.
     * @param direction direction.
     */
    protected BaseHasTypeAndDirection(RelationshipType type, Direction direction) {
        super(type);
        this.direction = direction;
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationships representation.
     */
    protected BaseHasTypeAndDirection(HasTypeAndDirection relationship) {
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
     * Does the {@link org.neo4j.graphdb.Direction} and {@link org.neo4j.graphdb.RelationshipType} of this relationship representation
     * equal to the direction and type of the given {@link org.neo4j.graphdb.Relationship}? Direction is taken from a
     * given {@link org.neo4j.graphdb.Node}'s point of view.
     *
     * @param relationship to compare.
     * @param pointOfView  Node whose point of view the relationship direction is being determined.
     * @return true iff the {@link org.neo4j.graphdb.Direction} and {@link org.neo4j.graphdb.RelationshipType} of this component matches
     *         the direction and type of the given {@link org.neo4j.graphdb.Relationship}.
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
        return super.matches(hasTypeAndDirection) && DirectionUtils.matches(getDirection(), hasTypeAndDirection.getDirection());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseHasTypeAndDirection that = (BaseHasTypeAndDirection) o;

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
