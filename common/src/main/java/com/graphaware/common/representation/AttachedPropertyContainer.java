package com.graphaware.common.representation;

import com.graphaware.common.expression.SupportsPropertyContainerExpressions;
import com.graphaware.common.util.PropertyContainerUtils;
import org.neo4j.graphdb.PropertyContainer;

public abstract class AttachedPropertyContainer<T extends PropertyContainer> implements SupportsPropertyContainerExpressions<Long> {

    protected final T propertyContainer;

    public AttachedPropertyContainer(T propertyContainer) {
        this.propertyContainer = propertyContainer;
    }

    @Override
    public Long getId() {
        return PropertyContainerUtils.id(propertyContainer);
    }

    @Override
    public boolean hasProperty(String key) {
        return propertyContainer.hasProperty(key);
    }

    @Override
    public Object getProperty(String key) {
        return propertyContainer.getProperty(key);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        return propertyContainer.getProperty(key, defaultValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachedPropertyContainer<?> that = (AttachedPropertyContainer<?>) o;

        return propertyContainer != null ? propertyContainer.equals(that.propertyContainer) : that.propertyContainer == null;

    }

    @Override
    public int hashCode() {
        return propertyContainer != null ? propertyContainer.hashCode() : 0;
    }
}
