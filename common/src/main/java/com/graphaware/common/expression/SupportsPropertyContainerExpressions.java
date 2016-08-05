package com.graphaware.common.expression;

public interface SupportsPropertyContainerExpressions<ID> {

    ID getId();

    boolean hasProperty(String key);

    Object getProperty(String key);

    Object getProperty(String key, Object defaultValue);
}
