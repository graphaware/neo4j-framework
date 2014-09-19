package com.graphaware.common.policy.spel;

import org.neo4j.graphdb.PropertyContainer;

/**
 * {@link PropertyContainer} wrapper that defines delegating methods usable in SPEL expressions when constructing
 * {@link SpelInclusionPolicy}s.
 */
abstract class PropertyContainerExpressions<T extends PropertyContainer> {

    protected final T propertyContainer;

    PropertyContainerExpressions(T propertyContainer) {
        this.propertyContainer = propertyContainer;
    }

    public boolean hasProperty(String key) {
        return propertyContainer.hasProperty(key);
    }

    public Object getProperty(String key, Object defaultValue) {
        return propertyContainer.getProperty(key, defaultValue);
    }
}
