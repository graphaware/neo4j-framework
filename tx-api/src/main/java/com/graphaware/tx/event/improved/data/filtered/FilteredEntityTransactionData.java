/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.tx.event.improved.data.filtered;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.inclusion.EntityInclusionPolicy;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNone;
import com.graphaware.common.util.Change;
import com.graphaware.tx.event.improved.data.EntityTransactionData;
import org.neo4j.graphdb.Entity;

import java.util.*;

/**
 * Decorator of {@link com.graphaware.tx.event.improved.data.EntityTransactionData} that filters out {@link org.neo4j.graphdb.Entity}s and properties
 * based on provided {@link EntityInclusionPolicy} and {@link PropertyInclusionPolicy}.
 * <p/>
 * Results of most methods returning {@link java.util.Collection}s and {@link java.util.Map}s will be filtered. <code>boolean</code>
 * and single object returning methods (and {@link #propertiesOfDeletedEntity(org.neo4j.graphdb.Entity)}
 * will always return the full truth no matter the policies. All returned {@link org.neo4j.graphdb.Entity}s will be wrapped
 * in {@link com.graphaware.tx.event.improved.entity.filtered.FilteredEntity}.
 * <p/>
 * So for example:
 * <p/>
 * {@link #getAllCreated()} can return 5 objects, but {@link #hasBeenCreated(org.neo4j.graphdb.Entity)} can
 * return true for more of them, as it ignores the filtering.
 * <p/>
 * When traversing the graph using an object returned by this API (such as {@link com.graphaware.tx.event.improved.entity.filtered.FilteredNode}),
 * nodes, properties, and relationships not included by the {@link InclusionPolicies} will be excluded. The only exception
 * to this are relationship start and end nodes - they are returned even if they would normally be filtered out. This is
 * a design decision in order to honor the requirement that relationships must have start and end node.
 */
public abstract class FilteredEntityTransactionData<T extends Entity> {

    protected final InclusionPolicies policies;

    /**
     * Construct filtered entity transaction data.
     *
     * @param policies for filtering.
     */
    protected FilteredEntityTransactionData(InclusionPolicies policies) {
        this.policies = policies;
    }

    /**
     * Get the wrapped/decorated {@link com.graphaware.tx.event.improved.data.EntityTransactionData}.
     *
     * @return wrapped object.
     */
    protected abstract EntityTransactionData<T> getWrapped();

    /**
     * Get entity inclusion policy for the appropriate entity.
     *
     * @return policy.
     */
    protected abstract EntityInclusionPolicy<T> getEntityInclusionPolicy();

    /**
     * Get property inclusion policy for the appropriate entity.
     *
     * @return policy.
     */
    protected abstract PropertyInclusionPolicy<T> getPropertyInclusionPolicy();

    /**
     * Create a filtered instance of a entity.
     *
     * @param original instance to be wrapped in the filtering decorator.
     * @return filtered instance.
     */
    protected abstract T filtered(T original);

    /**
     * Check whether the given entity has been created in the transaction.
     *
     * @param entity to check.
     * @return true iff the entity has been created. Full truth, no filtering performed.
     */
    public boolean hasBeenCreated(T entity) {
        return getWrapped().hasBeenCreated(entity);
    }

    /**
     * Get all entities created in the transaction.
     *
     * @return read-only collection of all created entities. Filtered according to provided policies.
     */
    public Collection<T> getAllCreated() {
        if (getEntityInclusionPolicy() instanceof IncludeNone) {
            return Collections.emptySet();
        }
        return filterEntities(getWrapped().getAllCreated());
    }

    /**
     * Check whether the given entity has been deleted in the transaction.
     *
     * @param entity to check.
     * @return true iff the entity has been deleted. Full truth, no filtering performed.
     */
    public boolean hasBeenDeleted(T entity) {
        return getWrapped().hasBeenDeleted(entity);
    }

    /**
     * Get a entity that has been deleted in this transaction as it was before the transaction started.
     *
     * @param entity to get.
     * @return snapshot of the entity before the transaction started. Filtering not applied on retrieving
     * the object, but the result is decorated by a
     * {@link com.graphaware.tx.event.improved.entity.filtered.FilteredEntity}.
     * @throws IllegalArgumentException in case the given entity has not been deleted in the transaction.
     */
    public T getDeleted(T entity) {
        return filtered(getWrapped().getDeleted(entity));
    }

    /**
     * Get all entities deleted in the transaction as they were before the transaction started.
     *
     * @return read-only collection of all deleted entities as they were before the transaction started
     * (snapshots). Filtered according to provided policies.
     */
    public Collection<T> getAllDeleted() {
        if (getEntityInclusionPolicy() instanceof IncludeNone) {
            return Collections.emptySet();
        }
        return filterEntities(getWrapped().getAllDeleted());
    }

    /**
     * Check whether a entity has been changed in the transaction, i.e. if any of its properties have been changed.
     *
     * @param entity to check.
     * @return true iff the entity has been changed. Full truth, no filtering performed.
     */
    public boolean hasBeenChanged(T entity) {
        return getWrapped().hasBeenChanged(entity);
    }

    /**
     * Get a entity that has been changed in this transaction as it was before the transaction started and as it is now.
     *
     * @param entity to get.
     * @return snapshot of the entity before the transaction started and the current state of the entity. Filtering not applied on retrieving the object, but the result is decorated by a
     * {@link com.graphaware.tx.event.improved.entity.filtered.FilteredEntity}.
     * @throws IllegalArgumentException in case the given entity has not been changed in the transaction.
     */
    public Change<T> getChanged(T entity) {
        return filteredChange(getWrapped().getChanged(entity));
    }

    /**
     * Get all entities changed in the transaction.
     *
     * @return a read-only collection of all changed entities as they were before the transaction started and
     * as they are now. Filtered according to provided policies.
     */
    public Collection<Change<T>> getAllChanged() {
        if (getEntityInclusionPolicy() instanceof IncludeNone) {
            return Collections.emptySet();
        }
        return filterChangedEntities(getWrapped().getAllChanged());
    }

    /**
     * Check whether a property has been created in the transaction.
     *
     * @param entity to check.
     * @param key       of the property to check.
     * @return true iff the property has been created. Full truth, no filtering performed.
     */
    public boolean hasPropertyBeenCreated(T entity, String key) {
        return getWrapped().hasPropertyBeenCreated(entity, key);
    }

    /**
     * Get properties created in the transaction.
     *
     * @param entity for which to get created properties.
     * @return read-only properties created for the given entity. Filtered according to provided
     * {@link PropertyInclusionPolicy}. Compliance with the {@link EntityInclusionPolicy} is not
     * verified.
     */
    public Map<String, Object> createdProperties(T entity) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }

        return filterProperties(getWrapped().createdProperties(entity), entity);
    }

    /**
     * Check whether a property has been deleted in the transaction.
     *
     * @param entity to check.
     * @param key       of the property to check.
     * @return true iff the property has been deleted. Full truth, no filtering performed.
     */
    public boolean hasPropertyBeenDeleted(T entity, String key) {
        return getWrapped().hasPropertyBeenDeleted(entity, key);
    }

    /**
     * Get properties deleted in the transaction.
     *
     * @param entity for which to get deleted properties.
     * @return read-only properties deleted for the given entity, where the value is the property value before the
     * transaction started. Filtered according to provided
     * {@link PropertyInclusionPolicy}. Compliance with the {@link EntityInclusionPolicy} is not
     * verified.
     */
    public Map<String, Object> deletedProperties(T entity) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }
        return filterProperties(getWrapped().deletedProperties(entity), entity);
    }

    /**
     * Get properties of a deleted entity.
     *
     * @param entity deleted entity.
     * @return read-only properties of the deleted entity, where the value is the property value before the
     * transaction started. Full truth, no filtering performed (because this is a Neo4j API workaround, not available through the public API (api package)).
     */
    public Map<String, Object> propertiesOfDeletedEntity(T entity) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }
        return filterProperties(getWrapped().propertiesOfDeletedEntity(entity), entity);
    }

    /**
     * Check whether a property has been changed in the transaction.
     *
     * @param entity to check.
     * @param key       of the property to check.
     * @return true iff the property has been changed. Full truth, no filtering performed.
     */
    public boolean hasPropertyBeenChanged(T entity, String key) {
        return getWrapped().hasPropertyBeenChanged(entity, key);
    }

    /**
     * Get properties changed in the transaction.
     *
     * @param entity for which to get changed properties.
     * @return read-only properties changed for the given entity, where the value is the property value before and
     * after the transaction started, respectively. Filtered according to provided
     * {@link PropertyInclusionPolicy}. Compliance with the {@link EntityInclusionPolicy} is not
     * verified.
     */
    public Map<String, Change<Object>> changedProperties(T entity) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }
        return filterProperties(getWrapped().changedProperties(entity), entity);
    }

    /**
     * Filter entities according to provided {@link EntityInclusionPolicy}.
     *
     * @param toFilter entities to filter.
     * @return filtered entities.
     */
    protected final Collection<T> filterEntities(Collection<T> toFilter) {
        Collection<T> result = new HashSet<>();
        for (T candidate : toFilter) {
            if (getEntityInclusionPolicy().include(candidate)) {
                result.add(filtered(candidate));
            }
        }
        return result;
    }

    /**
     * Filter changed entities according to provided policies. Only those complying with the provided
     * {@link EntityInclusionPolicy} with at least one property created, deleted, or changed that complies
     * with the provided {@link PropertyInclusionPolicy} will be returned.
     *
     * @param toFilter changed entities to filter.
     * @return filtered changed entities.
     */
    protected final Collection<Change<T>> filterChangedEntities(Collection<Change<T>> toFilter) {
        Collection<Change<T>> result = new HashSet<>();
        for (Change<T> candidate : toFilter) {
            if (include(candidate) && hasChanged(candidate)) {
                result.add(filteredChange(candidate));
            }
        }
        return result;
    }

    private boolean include(Change<T> candidate) {
        return getEntityInclusionPolicy().include(candidate.getPrevious()) || getEntityInclusionPolicy().include(candidate.getCurrent());
    }

    protected boolean hasChanged(Change<T> candidate) {
        return !createdProperties(candidate.getPrevious()).isEmpty()
                || !deletedProperties(candidate.getPrevious()).isEmpty()
                || !changedProperties(candidate.getPrevious()).isEmpty();
    }

    /**
     * Filter properties according to provided {@link PropertyInclusionPolicy}.
     * {@link EntityInclusionPolicy} is ignored!
     *
     * @param properties to filter.
     * @param entity  to which the properties belong.
     * @param <V>        property value type.
     * @return filtered properties.
     */
    protected final <V> Map<String, V> filterProperties(Map<String, V> properties, T entity) {
        Map<String, V> result = new HashMap<>();
        for (Map.Entry<String, V> entry : properties.entrySet()) {
            if (getPropertyInclusionPolicy().include(entry.getKey(), entity)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Make both objects contained in the changed object filtered.
     *
     * @param change to decorate with filtering entities.
     * @return decorated change.
     */
    protected final Change<T> filteredChange(Change<T> change) {
        return new Change<>(filtered(change.getPrevious()), filtered(change.getCurrent()));
    }
}
