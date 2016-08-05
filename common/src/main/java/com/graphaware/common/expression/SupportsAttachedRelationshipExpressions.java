package com.graphaware.common.expression;

public interface SupportsAttachedRelationshipExpressions<ID, N extends SupportsAttachedNodeExpressions> extends SupportsDetachedRelationshipExpressions<ID, N> {

    N getStartNode();

    N getEndNode();

    N pointOfView();
}
