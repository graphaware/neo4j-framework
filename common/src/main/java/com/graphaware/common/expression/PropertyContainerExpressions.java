package com.graphaware.common.expression;

import java.util.Map;

public interface PropertyContainerExpressions {

    Map<String, Object> getProperties();

    default boolean hasProperty(String key) {
        return getProperties() != null && getProperties().containsKey(key);
    }

    default Object getProperty(String key) {
        if (!hasProperty(key)) {
            return null;
        }
        return getProperties().get(key);
    }

    default Object getProperty(String key, Object defaultValue) {
        if (!hasProperty(key)) {
            return defaultValue;
        }

        return getProperty(key);
    }

}
