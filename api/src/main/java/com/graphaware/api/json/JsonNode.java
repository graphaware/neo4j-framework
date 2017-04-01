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

package com.graphaware.api.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.graphaware.api.SerializableNode;
import com.graphaware.common.transform.NodeIdTransformer;
import org.neo4j.graphdb.Node;

import java.util.Map;

/**
 * JSON-{@link SerializableNode}.
 *
 * @param <ID> type of custom node ID.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class JsonNode<ID> extends SerializableNode<ID> {

    public JsonNode() {
    }

    public JsonNode(Node node, NodeIdTransformer<ID> transformer) {
        super(node, transformer);
    }

    public JsonNode(Node node, String[] properties, NodeIdTransformer<ID> transformer) {
        super(node, properties, transformer);
    }

    public JsonNode(ID id) {
        super(id);
    }

    public JsonNode(ID id, String[] labels, Map<String, Object> properties) {
        super(id, labels, properties);
    }

    @JsonIgnore
    @Override
    public long getGraphId() {
        return super.getGraphId();
    }
}
