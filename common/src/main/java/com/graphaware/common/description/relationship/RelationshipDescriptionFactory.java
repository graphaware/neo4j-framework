/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.description.relationship;

import com.graphaware.common.description.predicate.Predicate;
import com.graphaware.common.description.property.LiteralPropertiesDescription;
import com.graphaware.common.description.property.WildcardPropertiesDescription;
import com.graphaware.common.util.DirectionUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;

import static org.neo4j.graphdb.RelationshipType.*;

/**
 * Factory for {@link RelationshipDescription}s.
 */
public final class RelationshipDescriptionFactory {

    private RelationshipDescriptionFactory() {
    }

    /**
     * Construct a new "literal" relationship description, i.e., one that treats properties that are not explicitly
     * constrained by a {@link Predicate} as {@link com.graphaware.common.description.predicate.Undefined}.
     * <p/>
     * This will be the most specific description of the given relationship, i.e., all property constraints will be taken
     * from the relationship properties and constrained to {@link com.graphaware.common.description.predicate.EqualTo} the actual
     * value on the relationship.
     *
     * @param relationship to create a description of.
     * @param pointOfView  node that is looking at this relationship for the purposes of determining direction. Must be one
     *                     of the participating nodes.
     * @return relationship description.
     */
    public static DetachedRelationshipDescription literal(Relationship relationship, Node pointOfView) {
        return new DetachedRelationshipDescriptionImpl(relationship.getType().name(), DirectionUtils.resolveDirection(relationship, pointOfView), new LiteralPropertiesDescription(relationship));
    }

    /**
     * Construct a new "literal" relationship description, i.e., one that treats properties that are not explicitly
     * constrained by a {@link Predicate} as {@link com.graphaware.common.description.predicate.Undefined}.
     *
     * @param type      of the relationship.
     * @param direction of the relationship.
     * @return relationship description.
     */
    public static DetachedRelationshipDescription literal(RelationshipType type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(type.name(), direction, new LiteralPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Construct a new "literal" relationship description, i.e., one that treats properties that are not explicitly
     * constrained by a {@link Predicate} as {@link com.graphaware.common.description.predicate.Undefined}.
     *
     * @param type      of the relationship.
     * @param direction of the relationship.
     * @return relationship description.
     */
    public static DetachedRelationshipDescription literal(String type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(type, direction, new LiteralPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Construct a new "wildcard" relationship description, i.e., one that treats properties that are not explicitly
     * constrained by a {@link Predicate} as {@link com.graphaware.common.description.predicate.Any} (wildcard).
     * <p/>
     * All property constraints will be taken from the relationship properties and constrained to {@link com.graphaware.common.description.predicate.EqualTo} the actual
     * value on the relationship.
     *
     * @param relationship to create a description of.
     * @param pointOfView  node that is looking at this relationship for the purposes of determining direction. Must be one
     *                     of the participating nodes.
     * @return relationship description.
     */
    public static DetachedRelationshipDescription wildcard(Relationship relationship, Node pointOfView) {
        return new DetachedRelationshipDescriptionImpl(relationship.getType().name(), DirectionUtils.resolveDirection(relationship, pointOfView), new WildcardPropertiesDescription(relationship));
    }

    /**
     * Construct a new "wildcard" relationship description, i.e., one that treats properties that are not explicitly
     * constrained by a {@link Predicate} as {@link com.graphaware.common.description.predicate.Any}.
     *
     * @param type      of the relationship.
     * @param direction of the relationship.
     * @return relationship description.
     */
    public static DetachedRelationshipDescription wildcard(RelationshipType type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(type.name(), direction, new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    /**
     * Construct a new "wildcard" relationship description, i.e., one that treats properties that are not explicitly
     * constrained by a {@link Predicate} as {@link com.graphaware.common.description.predicate.Any}.
     *
     * @param type      of the relationship.
     * @param direction of the relationship.
     * @return relationship description.
     */
    public static DetachedRelationshipDescription wildcard(String type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(type, direction, new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }
}
