package com.graphaware.common.representation;

import com.graphaware.common.expression.AttachedRelationshipExpressions;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class AttachedRelationship extends AttachedEntity<Relationship> implements AttachedRelationshipExpressions<AttachedNode> {

    private final Node pointOfView;

    public AttachedRelationship(Relationship entity) {
        this(entity, null);
    }

    public AttachedRelationship(Relationship entity, Node pointOfView) {
        super(entity);
        this.pointOfView = pointOfView;
    }

    @Override
    public String getType() {
        return entity.getType().name();
    }

    @Override
    public AttachedNode getStartNode() {
        return new AttachedNode(entity.getStartNode());
    }

    @Override
    public AttachedNode getEndNode() {
        return new AttachedNode(entity.getEndNode());
    }

    @Override
    public AttachedNode pointOfView() {
        return new AttachedNode(pointOfView);
    }
}
