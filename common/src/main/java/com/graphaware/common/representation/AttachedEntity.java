package com.graphaware.common.representation;

import com.graphaware.common.expression.EntityExpressions;
import org.neo4j.graphdb.Entity;

import java.util.Map;

public abstract class AttachedEntity<T extends Entity> implements EntityExpressions {

    protected final T entity;

    public AttachedEntity(T entity) {
        this.entity = entity;
    }
    @Override
    public Map<String, Object> getProperties() {
        return entity.getAllProperties();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachedEntity<?> that = (AttachedEntity<?>) o;

        return entity != null ? entity.equals(that.entity) : that.entity == null;

    }

    @Override
    public int hashCode() {
        return entity != null ? entity.hashCode() : 0;
    }
}
