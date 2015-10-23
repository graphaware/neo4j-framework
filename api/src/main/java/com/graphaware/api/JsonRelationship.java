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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.graphaware.common.representation.RelationshipRepresentation;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

/**
 * JSON-serializable {@link RelationshipRepresentation}.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class JsonRelationship extends RelationshipRepresentation {

    /**
     * Public no-arg constructor (for Jackson)
     */
    public JsonRelationship() {
    }

    /**
     * Create a JSON-serializable representation from a Neo4j relationship.
     *
     * @param relationship to create JSON from.
     */
    public JsonRelationship(Relationship relationship) {
        this(relationship, new JsonInput());
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node.
     *
     * @param relationship to create JSON from.
     * @param jsonInput    specifying what to include in the produced JSON.
     */
    public JsonRelationship(Relationship relationship, JsonInput jsonInput) {
        super(relationship, jsonInput.getRelationshipProperties());
    }

    /**
     * Construct a representation of a relationship from its internal Neo4j ID.
     *
     * @param id ID.
     */
    public JsonRelationship(long id) {
        super(id);
    }

    /**
     * Construct a new relationship representation.
     *
     * @param startNodeId start node ID.
     * @param endNodeId   end node ID.
     * @param type        relationship type.
     * @param properties  relationship properties.
     */
    public JsonRelationship(long startNodeId, long endNodeId, String type, Map<String, Object> properties) {
        super(startNodeId, endNodeId, type, properties);
    }
}

