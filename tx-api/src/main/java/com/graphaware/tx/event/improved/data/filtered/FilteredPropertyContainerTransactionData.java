/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
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
import com.graphaware.common.policy.inclusion.PropertyContainerInclusionPolicy;
import com.graphaware.common.policy.inclusion.PropertyInclusionPolicy;
import com.graphaware.common.policy.inclusion.none.IncludeNoProperties;
import com.graphaware.common.policy.inclusion.none.IncludeNone;
import com.graphaware.common.util.Change;
import com.graphaware.tx.event.improved.data.PropertyContainerTransactionData;
import org.neo4j.graphdb.PropertyContainer;

import java.util.*;

/**
 * Decorator of {@link com.graphaware.tx.event.improved.data.PropertyContainerTransactionData} that filters out {@link org.neo4j.graphdb.PropertyContainer}s and properties
 * based on provided {@link PropertyContainerInclusionPolicy} and {@link PropertyInclusionPolicy}.
 * <p/>
 * Results of most methods returning {@link java.util.Collection}s and {@link java.util.Map}s will be filtered. <code>boolean</code>
 * and single object returning methods (and {@link #propertiesOfDeletedContainer(org.neo4j.graphdb.PropertyContainer)}
 * will always return the full truth no matter the policies. All returned {@link org.neo4j.graphdb.PropertyContainer}s will be wrapped
 * in {@link com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredPropertyContainer}.
 * <p/>
 * So for example:
 * <p/>
 * {@link #getAllCreated()} can return 5 objects, but {@link #hasBeenCreated(org.neo4j.graphdb.PropertyContainer)} can
 * return true for more of them, as it ignores the filtering.
 * <p/>
 * When traversing the graph using an object returned by this API (such as {@link com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredNode}),
 * nodes, properties, and relationships not included by the {@link InclusionPolicies} will be excluded. The only exception
 * to this are relationship start and end nodes - they are returned even if they would normally be filtered out. This is
 * a design decision in order to honor the requirement that relationships must have start and end node.
 */
public abstract class FilteredPropertyContainerTransactionData<T extends PropertyContainer> {

    protected final InclusionPolicies policies;

    /**
     * Construct filtered property container transaction data.
     *
     * @param policies for filtering.
     */
    protected FilteredPropertyContainerTransactionData(InclusionPolicies policies) {
        this.policies = policies;
    }

    /**
     * Get the wrapped/decorated {@link com.graphaware.tx.event.improved.data.PropertyContainerTransactionData}.
     *
     * @return wrapped object.
     */
    protected abstract PropertyContainerTransactionData<T> getWrapped();

    /**
     * Get property container inclusion policy for the appropriate property container.
     *
     * @return policy.
     */
    protected abstract PropertyContainerInclusionPolicy<T> getPropertyContainerInclusionPolicy();

    /**
     * Get property inclusion policy for the appropriate property container.
     *
     * @return policy.
     */
    protected abstract PropertyInclusionPolicy<T> getPropertyInclusionPolicy();

    /**
     * Create a filtered instance of a container.
     *
     * @param original instance to be wrapped in the filtering decorator.
     * @return filtered instance.
     */
    protected abstract T filtered(T original);

    /**
     * Check whether the given property container has been created in the transaction.
     *
     * @param container to check.
     * @return true iff the property container has been created. Full truth, no filtering performed.
     */
    public boolean hasBeenCreated(T container) {
        return getWrapped().hasBeenCreated(container);
    }

    /**
     * Get all property containers created in the transaction.
     *
     * @return read-only collection of all created property containers. Filtered according to provided policies.
     */
    public Collection<T> getAllCreated() {
        if (getPropertyContainerInclusionPolicy() instanceof IncludeNone) {
            return Collections.emptySet();
        }
        return filterPropertyContainers(getWrapped().getAllCreated());
    }

    /**
     * Check whether the given property container has been deleted in the transaction.
     *
     * @param container to check.
     * @return true iff the property container has been deleted. Full truth, no filtering performed.
     */
    public boolean hasBeenDeleted(T container) {
        return getWrapped().hasBeenDeleted(container);
    }

    /**
     * Get a property container that has been deleted in this transaction as it was before the transaction started.
     *
     * @param container to get.
     * @return snapshot of the property container before the transaction started. Filtering not applied on retrieving
     * the object, but the result is decorated by a
     * {@link com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredPropertyContainer}.
     * @throws IllegalArgumentException in case the given property container has not been deleted in the transaction.
     */
    public T getDeleted(T container) {
        return filtered(getWrapped().getDeleted(container));
    }

    /**
     * Get all property containers deleted in the transaction as they were before the transaction started.
     *
     * @return read-only collection of all deleted property containers as they were before the transaction started
     * (snapshots). Filtered according to provided policies.
     */
    public Collection<T> getAllDeleted() {
        if (getPropertyContainerInclusionPolicy() instanceof IncludeNone) {
            return Collections.emptySet();
        }
        return filterPropertyContainers(getWrapped().getAllDeleted());
    }

    /**
     * Check whether a property container has been changed in the transaction, i.e. if any of its properties have been changed.
     *
     * @param container to check.
     * @return true iff the property container has been changed. Full truth, no filtering performed.
     */
    public boolean hasBeenChanged(T container) {
        return getWrapped().hasBeenChanged(container);
    }

    /**
     * Get a property container that has been changed in this transaction as it was before the transaction started and as it is now.
     *
     * @param container to get.
     * @return snapshot of the property container before the transaction started and the current state of the property
     * container. Filtering not applied on retrieving the object, but the result is decorated by a
     * {@link com.graphaware.tx.event.improved.propertycontainer.filtered.FilteredPropertyContainer}.
     * @throws IllegalArgumentException in case the given property container has not been changed in the transaction.
     */
    public Change<T> getChanged(T container) {
        return filteredChange(getWrapped().getChanged(container));
    }

    /**
     * Get all property containers changed in the transaction.
     *
     * @return a read-only collection of all changed property containers as they were before the transaction started and
     * as they are now. Filtered according to provided policies.
     */
    public Collection<Change<T>> getAllChanged() {
        if (getPropertyContainerInclusionPolicy() instanceof IncludeNone) {
            return Collections.emptySet();
        }
        return filterChangedPropertyContainers(getWrapped().getAllChanged());
    }

    /**
     * Check whether a property has been created in the transaction.
     *
     * @param container to check.
     * @param key       of the property to check.
     * @return true iff the property has been created. Full truth, no filtering performed.
     */
    public boolean hasPropertyBeenCreated(T container, String key) {
        return getWrapped().hasPropertyBeenCreated(container, key);
    }

    /**
     * Get properties created in the transaction.
     *
     * @param container for which to get created properties.
     * @return read-only properties created for the given container. Filtered according to provided
     * {@link PropertyInclusionPolicy}. Compliance with the {@link PropertyContainerInclusionPolicy} is not
     * verified.
     */
    public Map<String, Object> createdProperties(T container) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }

        return filterProperties(getWrapped().createdProperties(container), container);
    }

    /**
     * Check whether a property has been deleted in the transaction.
     *
     * @param container to check.
     * @param key       of the property to check.
     * @return true iff the property has been deleted. Full truth, no filtering performed.
     */
    public boolean hasPropertyBeenDeleted(T container, String key) {
        return getWrapped().hasPropertyBeenDeleted(container, key);
    }

    /**
     * Get properties deleted in the transaction.
     *
     * @param container for which to get deleted properties.
     * @return read-only properties deleted for the given container, where the value is the property value before the
     * transaction started. Filtered according to provided
     * {@link PropertyInclusionPolicy}. Compliance with the {@link PropertyContainerInclusionPolicy} is not
     * verified.
     */
    public Map<String, Object> deletedProperties(T container) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }
        return filterProperties(getWrapped().deletedProperties(container), container);
    }

    /**
     * Get properties of a deleted container.
     *
     * @param container deleted container.
     * @return read-only properties of the deleted container, where the value is the property value before the
     * transaction started. Full truth, no filtering performed (because this is a Neo4j API workaround, not available through the public API (api package)).
     */
    public Map<String, Object> propertiesOfDeletedContainer(T container) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }
        return filterProperties(getWrapped().propertiesOfDeletedContainer(container), container);
    }

    /**
     * Check whether a property has been changed in the transaction.
     *
     * @param container to check.
     * @param key       of the property to check.
     * @return true iff the property has been changed. Full truth, no filtering performed.
     */
    public boolean hasPropertyBeenChanged(T container, String key) {
        return getWrapped().hasPropertyBeenChanged(container, key);
    }

    /**
     * Get properties changed in the transaction.
     *
     * @param container for which to get changed properties.
     * @return read-only properties changed for the given container, where the value is the property value before and
     * after the transaction started, respectively. Filtered according to provided
     * {@link PropertyInclusionPolicy}. Compliance with the {@link PropertyContainerInclusionPolicy} is not
     * verified.
     */
    public Map<String, Change<Object>> changedProperties(T container) {
        if (getPropertyInclusionPolicy() instanceof IncludeNoProperties) {
            return Collections.emptyMap();
        }
        return filterProperties(getWrapped().changedProperties(container), container);
    }

    /**
     * Filter property containers according to provided {@link PropertyContainerInclusionPolicy}.
     *
     * @param toFilter property containers to filter.
     * @return filtered property containers.
     */
    protected final Collection<T> filterPropertyContainers(Collection<T> toFilter) {
        Collection<T> result = new HashSet<>();
        for (T candidate : toFilter) {
            if (getPropertyContainerInclusionPolicy().include(candidate)) {
                result.add(filtered(candidate));
            }
        }
        return result;
    }

    /**
     * Filter changed property containers according to provided policies. Only those complying with the provided
     * {@link PropertyContainerInclusionPolicy} with at least one property created, deleted, or changed that complies
     * with the provided {@link PropertyInclusionPolicy} will be returned.
     *
     * @param toFilter changed property containers to filter.
     * @return filtered changed property containers.
     */
    protected final Collection<Change<T>> filterChangedPropertyContainers(Collection<Change<T>> toFilter) {
        Collection<Change<T>> result = new HashSet<>();
        for (Change<T> candidate : toFilter) {
            if (include(candidate) && hasChanged(candidate)) {
                result.add(filteredChange(candidate));
            }
        }
        return result;
    }

    private boolean include(Change<T> candidate) {
        return getPropertyContainerInclusionPolicy().include(candidate.getPrevious()) || getPropertyContainerInclusionPolicy().include(candidate.getCurrent());
    }

    protected boolean hasChanged(Change<T> candidate) {
        return !createdProperties(candidate.getPrevious()).isEmpty()
                || !deletedProperties(candidate.getPrevious()).isEmpty()
                || !changedProperties(candidate.getPrevious()).isEmpty();
    }

    /**
     * Filter properties according to provided {@link PropertyInclusionPolicy}.
     * {@link PropertyContainerInclusionPolicy} is ignored!
     *
     * @param properties to filter.
     * @param container  to which the properties belong.
     * @param <V>        property value type.
     * @return filtered properties.
     */
    protected final <V> Map<String, V> filterProperties(Map<String, V> properties, T container) {
        Map<String, V> result = new HashMap<>();
        for (Map.Entry<String, V> entry : properties.entrySet()) {
            if (getPropertyInclusionPolicy().include(entry.getKey(), container)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Make both objects contained in the changed object filtered.
     *
     * @param change to decorate with filtering property containers.
     * @return decorated change.
     */
    protected final Change<T> filteredChange(Change<T> change) {
        return new Change<>(filtered(change.getPrevious()), filtered(change.getCurrent()));
    }
}
