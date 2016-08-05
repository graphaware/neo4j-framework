package com.graphaware.common.expression;

public interface SupportsAttachedNodeExpressions<ID> extends SupportsDetachedNodeExpressions<ID> {

    int getDegree();

    int getDegree(String typeOrDirection);

    int getDegree(String type, String direction);
}
