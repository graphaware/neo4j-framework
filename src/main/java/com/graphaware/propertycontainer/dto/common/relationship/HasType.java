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
 * Component that has a {@link org.neo4j.graphdb.RelationshipType}. Useful for property-less undirected {@link org.neo4j.graphdb.Relationship} representations.
 */
public interface HasType {

    /**
     * Get the relationship type.
     *
     * @return type.
     */
    RelationshipType getType();

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} of this instance equal to the type of the given {@link org.neo4j.graphdb.Relationship}?
     *
     * @param relationship to compare.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} of this instance matches the type of the given {@link org.neo4j.graphdb.Relationship}.
     */
    boolean matches(Relationship relationship);

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} of this instance equal to the type of the given {@link HasType}?
     *
     * @param hasType to compare.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} of this instance matches the type of the given {@link HasType}.
     */
    boolean matches(HasType hasType);
}
