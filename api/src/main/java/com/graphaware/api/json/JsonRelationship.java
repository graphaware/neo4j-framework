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
import com.graphaware.api.SerializableRelationship;
import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.transform.RelationshipIdTransformer;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

/**
 * JSON-{@link SerializableRelationship}.
 *
 * @param <ID> type of custom node/relationship IDs.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class JsonRelationship<ID> extends SerializableRelationship<ID> {

    public JsonRelationship() {
    }

    public JsonRelationship(Relationship relationship, RelationshipIdTransformer<ID> relationshipIdTransformer, NodeIdTransformer<ID> nodeIdTransformer) {
        super(relationship, relationshipIdTransformer, nodeIdTransformer);
    }

    public JsonRelationship(Relationship relationship, String[] properties, RelationshipIdTransformer<ID> relationshipIdTransformer, NodeIdTransformer<ID> nodeIdTransformer) {
        super(relationship, properties, relationshipIdTransformer, nodeIdTransformer);
    }

    public JsonRelationship(ID id) {
        super(id);
    }

    public JsonRelationship(ID id, ID startNodeId, ID endNodeId, String type, Map<String, Object> properties) {
        super(id, startNodeId, endNodeId, type, properties);
    }

    @JsonIgnore
    @Override
    public long getGraphId() {
        return super.getGraphId();
    }

    @JsonIgnore
    @Override
    public long getStartNodeGraphId() {
        return super.getStartNodeGraphId();
    }

    @JsonIgnore
    @Override
    public long getEndNodeGraphId() {
        return super.getEndNodeGraphId();
    }
}

