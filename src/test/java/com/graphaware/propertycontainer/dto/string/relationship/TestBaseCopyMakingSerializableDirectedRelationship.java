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

package com.graphaware.propertycontainer.dto.string.relationship;

import com.graphaware.propertycontainer.dto.common.relationship.ImmutableDirectedRelationship;
import com.graphaware.propertycontainer.dto.string.property.CopyMakingSerializablePropertiesImpl;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;

class TestBaseCopyMakingSerializableDirectedRelationship extends BaseCopyMakingSerializableDirectedRelationship<CopyMakingSerializablePropertiesImpl, TestBaseCopyMakingSerializableDirectedRelationship> implements ImmutableDirectedRelationship<String, CopyMakingSerializablePropertiesImpl>, CopyMakingSerializableDirectedRelationship<CopyMakingSerializablePropertiesImpl, TestBaseCopyMakingSerializableDirectedRelationship> {

    TestBaseCopyMakingSerializableDirectedRelationship(Relationship relationship, Node pointOfView) {
        super(relationship, pointOfView);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(Relationship relationship, Node pointOfView, CopyMakingSerializablePropertiesImpl properties) {
        super(relationship, pointOfView, properties);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(Relationship relationship, Node pointOfView, Map<String, ?> properties) {
        super(relationship, pointOfView, properties);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(RelationshipType type, Direction direction) {
        super(type, direction);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(RelationshipType type, Direction direction, CopyMakingSerializablePropertiesImpl properties) {
        super(type, direction, properties);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(RelationshipType type, Direction direction, Map<String, ?> properties) {
        super(type, direction, properties);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(String string, String separator) {
        super(string, separator);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(String string, String prefix, String separator) {
        super(string, prefix, separator);
    }

    TestBaseCopyMakingSerializableDirectedRelationship(ImmutableDirectedRelationship<String, CopyMakingSerializablePropertiesImpl> relationship) {
        super(relationship);
    }

    @Override
    protected TestBaseCopyMakingSerializableDirectedRelationship newRelationship(RelationshipType type, Direction direction, Map<String, ?> properties) {
        return new TestBaseCopyMakingSerializableDirectedRelationship(type, direction, properties);
    }

    @Override
    protected CopyMakingSerializablePropertiesImpl newProperties(Map<String, ?> properties) {
        return new CopyMakingSerializablePropertiesImpl(properties);
    }
}
