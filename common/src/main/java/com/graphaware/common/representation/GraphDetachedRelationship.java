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
