package com.graphaware.common.expression;

public interface DetachedRelationshipExpressions extends PropertyContainerExpressions {

    String getType();

    default boolean isType(String type) {
        return type.equals(getType());
    }
}
