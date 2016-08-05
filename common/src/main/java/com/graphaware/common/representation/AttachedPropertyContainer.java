package com.graphaware.common.representation;

import com.graphaware.common.expression.PropertyContainerExpressions;
import org.neo4j.graphdb.PropertyContainer;

import java.util.Map;

public abstract class AttachedPropertyContainer<T extends PropertyContainer> implements PropertyContainerExpressions {

    protected final T propertyContainer;

    public AttachedPropertyContainer(T propertyContainer) {
        this.propertyContainer = propertyContainer;
    }
    @Override
    public Map<String, Object> getProperties() {
        return propertyContainer.getAllProperties();
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
