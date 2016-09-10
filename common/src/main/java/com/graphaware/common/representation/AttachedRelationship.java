package com.graphaware.common.representation;

import com.graphaware.common.expression.AttachedRelationshipExpressions;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class AttachedRelationship extends AttachedPropertyContainer<Relationship> implements AttachedRelationshipExpressions<AttachedNode> {

    private final Node pointOfView;

    public AttachedRelationship(Relationship propertyContainer) {
        this(propertyContainer, null);
    }

    public AttachedRelationship(Relationship propertyContainer, Node pointOfView) {
        super(propertyContainer);
        this.pointOfView = pointOfView;
    }

    @Override
    public String getType() {
        return propertyContainer.getType().name();
    }

    @Override
    public AttachedNode getStartNode() {
        return new AttachedNode(propertyContainer.getStartNode());
    }

    @Override
    public AttachedNode getEndNode() {
        return new AttachedNode(propertyContainer.getEndNode());
    }

    @Override
    public AttachedNode pointOfView() {
        return new AttachedNode(pointOfView);
    }
}
