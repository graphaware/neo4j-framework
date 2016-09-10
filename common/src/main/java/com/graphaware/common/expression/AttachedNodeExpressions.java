package com.graphaware.common.expression;

public interface AttachedNodeExpressions extends DetachedNodeExpressions {

    int getDegree();

    int getDegree(String typeOrDirection);

    int getDegree(String type, String direction);
}
