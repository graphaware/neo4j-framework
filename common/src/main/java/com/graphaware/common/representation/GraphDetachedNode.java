package com.graphaware.common.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.graphdb.Node;

import java.util.Map;

public class GraphDetachedNode extends DetachedNode<Long> {

    public GraphDetachedNode() {
    }

    public GraphDetachedNode(Node node) {
        super(node);
    }

    public GraphDetachedNode(Node node, String[] properties) {
        super(node, properties);
    }

    public GraphDetachedNode(long graphId) {
        super(graphId);
    }

    public GraphDetachedNode(String[] labels, Map<String, Object> properties) {
        super(labels, properties);
    }

    public GraphDetachedNode(long graphId, String[] labels, Map<String, Object> properties) {
        super(graphId, labels, properties);
    }

    @JsonIgnore
    @Override
    public Long getId() {
        return getGraphId();
    }
}

