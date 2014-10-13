package com.graphaware.common.policy.spel;

import org.neo4j.graphdb.*;

import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.*;

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

    public int getDegree(String typeOrDirection) {
        try {
            return propertyContainer.getDegree(valueOf(typeOrDirection.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return propertyContainer.getDegree(withName(typeOrDirection));
        }
    }

    public int getDegree(String type, String direction) {
        return propertyContainer.getDegree(withName(type), valueOf(direction.toUpperCase()));
    }

    public boolean hasLabel(String label) {
        return propertyContainer.hasLabel(DynamicLabel.label(label));
    }
}
