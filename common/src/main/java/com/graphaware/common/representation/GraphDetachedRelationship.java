/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.graphaware.common.transform.NodeIdTransformer;
import com.graphaware.common.transform.TrivialNodeIdTransformer;
import org.neo4j.graphdb.Relationship;

import java.util.Map;

public class GraphDetachedRelationship extends DetachedRelationship<Long, GraphDetachedNode> {

    public GraphDetachedRelationship() {
    }

    public GraphDetachedRelationship(Relationship relationship) {
        super(relationship, TrivialNodeIdTransformer.getInstance());
    }

    public GraphDetachedRelationship(Relationship relationship, String[] properties) {
        super(relationship, properties, TrivialNodeIdTransformer.getInstance());
    }

    public GraphDetachedRelationship(long graphId) {
        super(graphId);
    }

    public GraphDetachedRelationship(long startNodeGraphId, long endNodeGraphId, String type, Map<String, Object> properties) {
        super(startNodeGraphId, endNodeGraphId, type, properties);
    }

    public GraphDetachedRelationship(long graphId, long startNodeGraphId, long endNodeGraphId, String type, Map<String, Object> properties) {
        super(graphId, startNodeGraphId, endNodeGraphId, type, properties);
    }

    @Override
    protected GraphDetachedNode startNode(Relationship relationship, NodeIdTransformer<Long> nodeIdTransformer) {
        return new GraphDetachedNode(relationship.getStartNode());
    }

    @Override
    protected GraphDetachedNode endNode(Relationship relationship, NodeIdTransformer<Long> nodeIdTransformer) {
        return new GraphDetachedNode(relationship.getEndNode());
    }

    @JsonIgnore
    @Override
    public Long getId() {
        return getGraphId();
    }
}
