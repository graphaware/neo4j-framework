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

package com.graphaware.description.relationship;

import com.graphaware.description.predicate.Predicate;
import com.graphaware.description.property.LiteralPropertiesDescription;
import com.graphaware.description.property.WildcardPropertiesDescription;
import com.graphaware.propertycontainer.util.DirectionUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Collections;

import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 *
 */
public final class RelationshipDescriptionFactory {

    private RelationshipDescriptionFactory() {
    }

    public static DetachedRelationshipDescription literal(Relationship relationship, Node pointOfView) {
        return new DetachedRelationshipDescriptionImpl(relationship.getType(), DirectionUtils.resolveDirection(relationship, pointOfView), new LiteralPropertiesDescription(relationship));
    }

    public static DetachedRelationshipDescription literal(RelationshipType type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(type, direction, new LiteralPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    public static DetachedRelationshipDescription literal(String type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(withName(type), direction, new LiteralPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    public static DetachedRelationshipDescription wildcard(Relationship relationship, Node pointOfView) {
        return new DetachedRelationshipDescriptionImpl(relationship.getType(), DirectionUtils.resolveDirection(relationship, pointOfView), new WildcardPropertiesDescription(relationship));
    }

    public static DetachedRelationshipDescription wildcard(RelationshipType type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(type, direction, new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }

    public static DetachedRelationshipDescription wildcard(String type, Direction direction) {
        return new DetachedRelationshipDescriptionImpl(withName(type), direction, new WildcardPropertiesDescription(Collections.<String, Predicate>emptyMap()));
    }
}
