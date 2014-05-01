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

package com.graphaware.api;

import com.graphaware.common.util.DirectionUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * JSON-serializable representation of a Neo4j relationship.
 */
public class JsonRelationship extends JsonPropertyContainer {

    private String type;
    private Direction direction = Direction.BOTH;

    public JsonRelationship(Relationship relationship, JsonInput jsonInput, Node pointOfView) {
        super(relationship.getId());

        if (jsonInput.getRelationshipProperties() != null) {
            for (String property : jsonInput.getRelationshipProperties()) {
                if (relationship.hasProperty(property)) {
                    putProperty(property, relationship.getProperty(property));
                }
            }
        }

        setType(relationship.getType().name());
        setDirection(DirectionUtils.resolveDirection(relationship, pointOfView));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
