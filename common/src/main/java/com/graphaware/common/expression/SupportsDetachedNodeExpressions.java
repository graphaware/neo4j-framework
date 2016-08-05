package com.graphaware.common.expression;

public interface SupportsDetachedNodeExpressions<ID> extends SupportsPropertyContainerExpressions<ID> {

    boolean hasLabel(String label);

    String[] getLabels();
}
