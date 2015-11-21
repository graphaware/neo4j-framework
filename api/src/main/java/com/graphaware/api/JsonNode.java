/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.graphaware.common.representation.NodeRepresentation;
import org.neo4j.graphdb.Node;

import java.util.Map;

/**
 * JSON-serializable {@link NodeRepresentation}.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class JsonNode extends NodeRepresentation {

    /**
     * Public no-arg constructor (for Jackson)
     */
    public JsonNode() {
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node.
     *
     * @param node node to create JSON from.
     */
    public JsonNode(Node node) {
        this(node, new JsonInput());
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node.
     *
     * @param node      node to create JSON from.
     * @param jsonInput specifying what to include in the produced JSON.
     */
    public JsonNode(Node node, JsonInput jsonInput) {
        super(node, jsonInput.getNodeProperties());
    }

    /**
     * Create a JSON-serializable representation from a Neo4j node ID.
     *
     * @param id of a node to create JSON from.
     */
    public JsonNode(long id) {
        super(id);
    }

    /**
     * Construct a new representation of a node.
     *
     * @param labels of the new node.
     * @param properties of the new node.
     */
    public JsonNode(String[] labels, Map<String, Object> properties) {
        super(labels, properties);
    }
}
