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

package com.graphaware.tx.event.improved.data.lazy;


import com.graphaware.common.util.Change;
import com.graphaware.tx.event.improved.data.PropertyContainerTransactionData;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.logging.Log;
import org.slf4j.Logger;
import com.graphaware.common.log.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.graphaware.common.util.PropertyContainerUtils.id;

/**
 * {@link com.graphaware.tx.event.improved.data.PropertyContainerTransactionData} that lazily initializes its internal structures (indexed transaction data)
 * as they are needed by callers to prevent unnecessary overheads.
 *
 * @param <T> type of the property container.
 */
public abstract class LazyPropertyContainerTransactionData<T extends PropertyContainer> implements PropertyContainerTransactionData<T> {
    private static final Log LOG = LoggerFactory.getLogger(LazyPropertyContainerTransactionData.class);

    private Map<Long, T> created = null;
    private Map<Long, T> deleted = null;
    private Map<Long, Change<T>> changed = null;

    /**
     * <ID, <key, new value>>
     */
    private Map<Long, Map<String, Object>> createdProperties = null;
    /**
     * <ID, <key, old value>>
     */
    private Map<Long, Map<String, Object>> deletedProperties = null;
    /**
     * <ID, <key, old and new value>>
     */
    private Map<Long, Map<String, Change<Object>>> changedProperties = null;
    /**
     * <ID, <key, old value>> of properties of deleted property containers
     */
    private Map<Long, Map<String, Object>> deletedContainersProperties = null;

    /**
     * Create an old snapshot of an original property container.
     *
     * @param original to create a snapshot from.
     * @return the snapshot.
     */
    protected abstract T oldSnapshot(T original);

    /**
     * Create a new snapshot of an original property container.
     *
     * @param original to create a snapshot from.
     * @return the snapshot.
     */
    protected abstract T newSnapshot(T original);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBeenCreated(T container) {
        initializeCreated();
        return created.containsKey(id(container));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> getAllCreated() {
        initializeCreated();
        return Collections.unmodifiableCollection(created.values());
    }

    private void initializeCreated() {
        if (created == null) {

            created = new HashMap<>();

            for (T created : created()) {
                this.created.put(id(created), newSnapshot(created));
            }
        }
    }

    /**
     * Get all property containers created in the transaction from the Neo4j API.
     *
     * @return created property containers.
     */
    protected abstract Iterable<T> created();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBeenDeleted(T container) {
        initializeDeleted();
        return deleted.containsKey(id(container));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getDeleted(T container) {
        initializeDeleted();

        if (!hasBeenDeleted(container)) {
            throw new IllegalArgumentException(container + " has not been deleted!");
        }

        return deleted.get(id(container));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<T> getAllDeleted() {
        initializeDeleted();
        return Collections.unmodifiableCollection(deleted.values());
    }

    private void initializeDeleted() {
        if (deleted == null) {

            deleted = new HashMap<>();

            for (T deleted : deleted()) {
                this.deleted.put(id(deleted), oldSnapshot(deleted));
            }
        }
    }

    /**
     * Get all property containers deleted in the transaction from the Neo4j API.
     *
     * @return created property containers.
     */
    protected abstract Iterable<T> deleted();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBeenChanged(T container) {
        initializeChanged();
        return changedContainsKey(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Change<T> getChanged(T container) {
        initializeChanged();

        if (!hasBeenChanged(container)) {
            throw new IllegalArgumentException(container + " has not been changed!");
        }

        return changed.get(id(container));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Change<T>> getAllChanged() {
        initializeChanged();
        return Collections.unmodifiableCollection(changed.values());
    }

    protected void initializeChanged() {
        initializeCreated();
        initializeDeleted();

        if (changed == null) {
            changed = new HashMap<>();

            for (PropertyEntry<T> propertyEntry : assignedProperties()) {
                if (hasNotActuallyChanged(propertyEntry)) {
                    continue;
                }

                T candidate = propertyEntry.entity();
                if (!hasBeenCreated(candidate)) {
                    registerChange(candidate);
                }
            }

            for (PropertyEntry<T> propertyEntry : removedProperties()) {
                T candidate = propertyEntry.entity();
                if (!hasBeenDeleted(candidate)) {
                    registerChange(candidate);
                }
            }

            doInitializeChanged();
        }
    }

    protected void doInitializeChanged() {
        //for subclasses
    }

    protected void registerChange(T candidate) {
        if (!changedContainsKey(candidate)) {
            Change<T> change = createChangeObject(candidate);
            changed.put(id(candidate), change);
        }
    }

    protected boolean changedContainsKey(T candidate) {
        return changed.containsKey(id(candidate));
    }

    protected Change<T> createChangeObject(T candidate) {
        return new Change<>(oldSnapshot(candidate), newSnapshot(candidate));
    }

    /**
     * Get all assigned properties from the Neo4j API.
     *
     * @return assigned properties.
     */
    protected abstract Iterable<PropertyEntry<T>> assignedProperties();

    /**
     * Get all removed properties from the Neo4j API.
     *
     * @return removed properties.
     */
    protected abstract Iterable<PropertyEntry<T>> removedProperties();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPropertyBeenCreated(T container, String key) {
        initializeProperties();

        if (!hasBeenChanged(container)) {
            LOG.warn(container + " has not been changed but the caller thinks it should have created properties.");
            return false;
        }

        if (!createdProperties.containsKey(id(container))) {
            return false;
        }

        return createdProperties.get(id(container)).containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createdProperties(T container) {
        initializeProperties();

        if (!hasBeenChanged(container)) {
            LOG.warn(container + " has not been changed but the caller thinks it should have created properties.");
            return Collections.emptyMap();
        }

        if (!createdProperties.containsKey(id(container))) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(createdProperties.get(id(container)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPropertyBeenDeleted(T container, String key) {
        initializeProperties();

        if (!hasBeenChanged(container)) {
            LOG.warn(container + " has not been changed but the caller thinks it should have deleted properties.");
            return false;
        }

        if (!deletedProperties.containsKey(id(container))) {
            return false;
        }

        return deletedProperties.get(id(container)).containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> deletedProperties(T container) {
        initializeProperties();

        if (!hasBeenChanged(container)) {
            LOG.warn(container + " has not been changed but the caller thinks it should have deleted properties.");
            return Collections.emptyMap();
        }

        if (!deletedProperties.containsKey(id(container))) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(deletedProperties.get(id(container)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> propertiesOfDeletedContainer(T container) {
        initializeProperties();

        if (!hasBeenDeleted(container)) {
            LOG.error(container + " has not been deleted but the caller thinks it has! This is a bug.");
            throw new IllegalStateException(container + " has not been deleted but the caller thinks it has! This is a bug.");
        }

        if (!deletedContainersProperties.containsKey(id(container))) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(deletedContainersProperties.get(id(container)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPropertyBeenChanged(T container, String key) {
        initializeProperties();

        if (!hasBeenChanged(container)) {
            LOG.warn(container + " has not been changed but the caller thinks it should have changed properties.");
            return false;
        }

        if (!changedProperties.containsKey(id(container))) {
            return false;
        }

        return changedProperties.get(id(container)).containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Change<Object>> changedProperties(T container) {
        initializeProperties();

        if (!hasBeenChanged(container)) {
            LOG.warn(container + " has not been changed but the caller thinks it should have changed properties.");
            return Collections.emptyMap();
        }

        if (!changedProperties.containsKey(id(container))) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(changedProperties.get(id(container)));
    }

    private void initializeProperties() {
        if (createdProperties != null) {
            assert changedProperties != null;
            assert deletedProperties != null;
            assert deletedContainersProperties != null;

            return;
        }

        //initializeCreated(); // - called by initializeChanged()
        //initializeDeleted(); // - called by initializeChanged()
        initializeChanged();

        createdProperties = new HashMap<>();
        deletedProperties = new HashMap<>();
        changedProperties = new HashMap<>();
        deletedContainersProperties = new HashMap<>();

        for (PropertyEntry<T> propertyEntry : assignedProperties()) {
            T container = propertyEntry.entity();

            if (hasBeenCreated(container)) {
                continue;
            }

            if (hasNotActuallyChanged(propertyEntry)) {
                continue;
            }

            if (propertyEntry.previouslyCommitedValue() == null) {
                if (!createdProperties.containsKey(id(container))) {
                    createdProperties.put(id(container), new HashMap<String, Object>());
                }
                createdProperties.get(id(container)).put(propertyEntry.key(), propertyEntry.value());
            } else {
                if (!changedProperties.containsKey(id(container))) {
                    changedProperties.put(id(container), new HashMap<String, Change<Object>>());
                }
                changedProperties.get(id(container)).put(propertyEntry.key(), new Change<>(propertyEntry.previouslyCommitedValue(), propertyEntry.value()));
            }
        }

        for (PropertyEntry<T> propertyEntry : removedProperties()) {
            T container = propertyEntry.entity();

            if (deleted.containsKey(id(container))) {
                if (!deletedContainersProperties.containsKey(id(container))) {
                    deletedContainersProperties.put(id(container), new HashMap<String, Object>());
                }
                deletedContainersProperties.get(id(container)).put(propertyEntry.key(), propertyEntry.previouslyCommitedValue());
                continue;
            }

            if (!changedContainsKey(container)) {
                throw new IllegalStateException(container + " seems to have not been deleted or changed, this is a bug");
            }

            assert changedContainsKey(container);

            if (!deletedProperties.containsKey(id(container))) {
                deletedProperties.put(id(container), new HashMap<String, Object>());
            }

            deletedProperties.get(id(container)).put(propertyEntry.key(), propertyEntry.previouslyCommitedValue());
        }
    }

    private boolean hasNotActuallyChanged(PropertyEntry<T> propertyEntry) {
        return propertyEntry.previouslyCommitedValue() != null && propertyEntry.previouslyCommitedValue().equals(propertyEntry.value());
    }
}
