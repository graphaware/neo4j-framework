package com.graphaware.common.expression;

import java.util.Arrays;
import java.util.HashSet;

public interface DetachedNodeExpressions extends PropertyContainerExpressions {

    String[] getLabels();

    default boolean hasLabel(String label) {
        return new HashSet<>(Arrays.asList(getLabels())).contains(label);
    }
}
