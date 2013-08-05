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

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * Base-class for {@link HasType} implementations.
 */
public abstract class BaseHasType {

    private final RelationshipType type;

    /**
     * Construct a relationship representation.
     *
     * @param relationship Neo4j relationship to represent.
     */
    protected BaseHasType(Relationship relationship) {
        this.type = relationship.getType();
    }

    /**
     * Construct a relationship representation.
     *
     * @param type type of represented relationship.
     */
    protected BaseHasType(RelationshipType type) {
        this.type = type;
    }

    /**
     * Construct a relationship representation from another one.
     *
     * @param relationship relationship representation.
     */
    protected BaseHasType(HasType relationship) {
        this.type = relationship.getType();
    }

    /**
     * Get the relationship type.
     *
     * @return type.
     */
    public RelationshipType getType() {
        return type;
    }

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} of this relationship representation equal to the type of the given {@link org.neo4j.graphdb.Relationship}?
     *
     * @param relationship to compare.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} of this relationship representation matches the type of the given {@link org.neo4j.graphdb.Relationship}.
     */
    public boolean matches(Relationship relationship) {
        return relationship.isType(getType());
    }

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} of this instance equal to the type of the given {@link HasType}?
     *
     * @param hasType to compare.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} of this instance matches the type of the given {@link HasType}.
     */
    public boolean matches(HasType hasType) {
        return getType().name().equals(hasType.getType().name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseHasType that = (BaseHasType) o;

        //noinspection RedundantIfStatement
        if (!type.name().equals(that.type.name())) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return type.name().hashCode();
    }
}
