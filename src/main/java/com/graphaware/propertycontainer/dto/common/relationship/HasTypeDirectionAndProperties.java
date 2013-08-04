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
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Component that has a {@link org.neo4j.graphdb.RelationshipType}, a {@link org.neo4j.graphdb.Direction}, and {@link ImmutableProperties}.
 * Useful for directed {@link org.neo4j.graphdb.Relationship} representations.
 *
 * @param <V> type with which property values are represented.
 * @param <P> type of properties contained.
 */
public interface HasTypeDirectionAndProperties<V, P extends ImmutableProperties<V>> extends HasTypeAndDirection, HasTypeAndProperties<V, P> {

    /**
     * Do the {@link org.neo4j.graphdb.RelationshipType}, {@link org.neo4j.graphdb.Direction}, and {@link ImmutableProperties}
     * of this instance match the type, direction, and properties of the given {@link org.neo4j.graphdb.Relationship}?
     * Matching types must have equal names; matching directions must be
     * equal, or at least one of them must be {@link org.neo4j.graphdb.Direction#BOTH}. Direction of the given {@link org.neo4j.graphdb.Relationship}
     * is taken from the given {@link org.neo4j.graphdb.Node}'s point of view. Matching properties must be equal.
     *
     * @param relationship to check match.
     * @param pointOfView  Node whose point of view the relationship direction is being determined.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType}, {@link org.neo4j.graphdb.Direction}, and
     *         {@link ImmutableProperties} of this instance match the type, direction, and properties of the given {@link org.neo4j.graphdb.Relationship}.
     */
    @Override
    //overridden for javadoc
    boolean matches(Relationship relationship, Node pointOfView);

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance equal to the
     * type and properties of the given {@link HasTypeDirectionAndProperties}?
     *
     * @param hasTypeDirectionAndProperties to check match.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance
     *         equal to the type and properties of the given {@link HasTypeDirectionAndProperties}.
     */
    boolean matches(HasTypeDirectionAndProperties<V, ?> hasTypeDirectionAndProperties);
}
