package com.graphaware.common.expression;

import com.graphaware.common.representation.AttachedNode;
import com.graphaware.common.representation.AttachedRelationship;

public class AttachedRelationshipPropertyExpressions extends RelationshipPropertyExpressions<AttachedNode, AttachedRelationship> {

    public AttachedRelationshipPropertyExpressions(String key, AttachedRelationship propertyContainer) {
        super(key, propertyContainer);
    }

    @Override
    public AttachedRelationship getRelationship() {
        return propertyContainer;
    }
}
