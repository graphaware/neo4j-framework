package com.graphaware.common.policy.spel;

import org.neo4j.graphdb.*;

/**
 * {@link PropertyContainerExpressions} for {@link Node}s.
 */
class NodeExpressions extends PropertyContainerExpressions<Node> {

    NodeExpressions(Node node) {
        super(node);
    }

    public int getDegree() {
        return propertyContainer.getDegree();
    }

    public int getDegree(String direction) {
        return propertyContainer.getDegree(Direction.valueOf(direction.toUpperCase()));
    }

    public int getDegree(String type, String direction) {
        return propertyContainer.getDegree(DynamicRelationshipType.withName(type), Direction.valueOf(direction.toUpperCase()));
    }

    public boolean hasLabel(String label) {
        return propertyContainer.hasLabel(DynamicLabel.label(label));
    }
}
