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
import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.transform.RelationshipIdTransformer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

/**
 * {@link JsonRelationship} with {@link String} custom ID.
 */
public class StringIdJsonRelationship extends JsonRelationship<String> {

    public StringIdJsonRelationship() {
    }

    public StringIdJsonRelationship(Relationship relationship, RelationshipIdTransformer<String> relationshipIdTransformer, NodeIdTransformer<String> nodeIdTransformer) {
        super(relationship, relationshipIdTransformer, nodeIdTransformer);
    }

    public StringIdJsonRelationship(Relationship relationship, String[] properties, RelationshipIdTransformer<String> relationshipIdTransformer, NodeIdTransformer<String> nodeIdTransformer) {
        super(relationship, properties, relationshipIdTransformer, nodeIdTransformer);
    }

    public StringIdJsonRelationship(String id) {
        super(id);
    }

    public StringIdJsonRelationship(String id, String startNodeId, String endNodeId, String type, Map<String, Object> properties) {
        super(id, startNodeId, endNodeId, type, properties);
    }

    @Override
    public Relationship producePropertyContainer(GraphDatabaseService database) {
        throw new UnsupportedOperationException("Please use producePropertyContainer(GraphDatabaseService database, RelationshipIdTransformer<ID> relationshipIdTransformer, NodeIdTransformer<ID> nodeIdTransformer)");
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

