package com.graphaware.common.expression;

import com.graphaware.common.representation.AttachedNode;

public class AttachedNodePropertyExpressions extends NodePropertyExpressions<AttachedNode> {

    public AttachedNodePropertyExpressions(String key, AttachedNode propertyContainer) {
        super(key, propertyContainer);
    }

    @Override
    public AttachedNode getNode() {
        return propertyContainer;
    }
}
