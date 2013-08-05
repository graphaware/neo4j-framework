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
import org.neo4j.graphdb.Relationship;

/**
 * Component that has a {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties}.
 * Useful for undirected {@link org.neo4j.graphdb.Relationship} representations.
 *
 * @param <V> type with which property values are represented.
 * @param <P> type of properties contained.
 */
public interface HasTypeAndProperties<V, P extends ImmutableProperties<V>> extends HasType, HasProperties<V, P> {

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance equal to the
     * type and properties of the given {@link org.neo4j.graphdb.Relationship}?
     *
     * @param relationship to compare.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance \
     *         match the type and properties of the given {@link org.neo4j.graphdb.Relationship}.
     */
    @Override
    //for javadoc
    boolean matches(Relationship relationship);

    /**
     * Does the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance equal to the
     * type and properties of the given {@link HasTypeAndProperties}?
     *
     * @param hasTypeAndProperties to check match.
     * @return true iff the {@link org.neo4j.graphdb.RelationshipType} and {@link ImmutableProperties} of this instance
     *         equal to the type and properties of the given {@link HasTypeAndProperties}.
     */
    boolean matches(HasTypeAndProperties<V, ?> hasTypeAndProperties);
}
