package com.graphaware.common.expression;

public interface SupportsDetachedRelationshipExpressions<ID, N extends SupportsDetachedNodeExpressions> extends SupportsPropertyContainerExpressions<ID> {

    String getType();

    boolean isType(String type);
}
