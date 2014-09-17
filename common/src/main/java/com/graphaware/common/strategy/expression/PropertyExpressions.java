package com.graphaware.common.strategy.expression;

import org.neo4j.graphdb.PropertyContainer;

/**
 * Property key and its containing {@link PropertyContainer} wrapper that defines delegating methods usable in SPEL
 * expressions when constructing {@link SpelInclusionStrategy}s.
 */
abstract class PropertyExpressions<T extends PropertyContainer> {

    protected final String key;
    protected final T propertyContainer;

    PropertyExpressions(String key, T propertyContainer) {
        this.key = key;
        this.propertyContainer = propertyContainer;
    }

    public String getKey() {
        return key;
    }
}
