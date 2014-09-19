package com.graphaware.common.policy.spel;

import org.neo4j.graphdb.Node;

/**
 *  {@link PropertyExpressions} for {@link Node} properties.
 */
class NodePropertyExpressions extends PropertyExpressions<Node> {

    NodePropertyExpressions(String key, Node node) {
        super(key, node);
    }

    public NodeExpressions getNode() {
        return new NodeExpressions(propertyContainer);
    }
}
