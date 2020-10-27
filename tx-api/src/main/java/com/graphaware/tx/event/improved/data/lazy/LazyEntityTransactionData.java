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

package com.graphaware.tx.event.improved.data.lazy;


import com.graphaware.common.util.Change;
import com.graphaware.tx.event.improved.data.EntityTransactionData;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link com.graphaware.tx.event.improved.data.EntityTransactionData} that lazily initializes its internal structures (indexed transaction data)
 * as they are needed by callers to prevent unnecessary overheads.
 *
 * @param <T> type of the entity.
 */
public abstract class LazyEntityTransactionData<T extends Entity> implements EntityTransactionData<T> {
    private static final Log LOG = LoggerFactory.getLogger(LazyEntityTransactionData.class);

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
     * <ID, <key, old value>> of properties of deleted entities
     */
    private Map<Long, Map<String, Object>> deletedEntityProperties = null;

    /**
     * Create an old snapshot of an original entity.
     *
     * @param original to create a snapshot from.
     * @return the snapshot.
     */
    protected abstract T oldSnapshot(T original);

    /**
     * Create a new snapshot of an original entity.
     *
     * @param original to create a snapshot from.
     * @return the snapshot.
     */
    protected abstract T newSnapshot(T original);

    @Override
    public boolean hasBeenCreated(T entity) {
        initializeCreated();
        return created.containsKey(entity.getId());
    }

    @Override
    public Collection<T> getAllCreated() {
        initializeCreated();
        return Collections.unmodifiableCollection(created.values());
    }

    private void initializeCreated() {
        if (created == null) {

            created = new HashMap<>();

            for (T created : created()) {
                this.created.put(created.getId(), newSnapshot(created));
            }
        }
    }

    /**
     * Get all entities created in the transaction from the Neo4j API.
     *
     * @return created entities.
     */
    protected abstract Iterable<T> created();

    @Override
    public boolean hasBeenDeleted(T entity) {
        initializeDeleted();
        return deleted.containsKey(entity.getId());
    }

    @Override
    public T getDeleted(T entity) {
        initializeDeleted();

        if (!hasBeenDeleted(entity)) {
            throw new IllegalArgumentException(entity + " has not been deleted!");
        }

        return deleted.get(entity.getId());
    }

    @Override
    public Collection<T> getAllDeleted() {
        initializeDeleted();
        return Collections.unmodifiableCollection(deleted.values());
    }

    private void initializeDeleted() {
        if (deleted == null) {

            deleted = new HashMap<>();

            for (T deleted : deleted()) {
                this.deleted.put(deleted.getId(), oldSnapshot(deleted));
            }
        }
    }

    /**
     * Get all entities deleted in the transaction from the Neo4j API.
     *
     * @return created entities.
     */
    protected abstract Iterable<T> deleted();

    @Override
    public boolean hasBeenChanged(T entity) {
        initializeChanged();
        return changedContainsKey(entity);
    }

    @Override
    public Change<T> getChanged(T entity) {
        initializeChanged();

        if (!hasBeenChanged(entity)) {
            throw new IllegalArgumentException(entity + " has not been changed!");
        }

        return changed.get(entity.getId());
    }

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
            changed.put(candidate.getId(), change);
        }
    }

    protected boolean changedContainsKey(T candidate) {
        return changed.containsKey(candidate.getId());
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

    @Override
    public boolean hasPropertyBeenCreated(T entity, String key) {
        initializeProperties();

        if (!hasBeenChanged(entity)) {
            LOG.warn(entity + " has not been changed but the caller thinks it should have created properties.");
            return false;
        }

        if (!createdProperties.containsKey(entity.getId())) {
            return false;
        }

        return createdProperties.get(entity.getId()).containsKey(key);
    }

    @Override
    public Map<String, Object> createdProperties(T entity) {
        initializeProperties();

        if (!hasBeenChanged(entity)) {
            LOG.warn(entity + " has not been changed but the caller thinks it should have created properties.");
            return Collections.emptyMap();
        }

        if (!createdProperties.containsKey(entity.getId())) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(createdProperties.get(entity.getId()));
    }

    @Override
    public boolean hasPropertyBeenDeleted(T entity, String key) {
        initializeProperties();

        if (!hasBeenChanged(entity)) {
            LOG.warn(entity + " has not been changed but the caller thinks it should have deleted properties.");
            return false;
        }

        if (!deletedProperties.containsKey(entity.getId())) {
            return false;
        }

        return deletedProperties.get(entity.getId()).containsKey(key);
    }

    @Override
    public Map<String, Object> deletedProperties(T entity) {
        initializeProperties();

        if (!hasBeenChanged(entity)) {
            LOG.warn(entity + " has not been changed but the caller thinks it should have deleted properties.");
            return Collections.emptyMap();
        }

        if (!deletedProperties.containsKey(entity.getId())) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(deletedProperties.get(entity.getId()));
    }

    @Override
    public Map<String, Object> propertiesOfDeletedEntity(T entity) {
        initializeProperties();

        if (!hasBeenDeleted(entity)) {
            LOG.error(entity + " has not been deleted but the caller thinks it has! This is a bug.");
            throw new IllegalStateException(entity + " has not been deleted but the caller thinks it has! This is a bug.");
        }

        if (!deletedEntityProperties.containsKey(entity.getId())) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(deletedEntityProperties.get(entity.getId()));
    }

    @Override
    public boolean hasPropertyBeenChanged(T entity, String key) {
        initializeProperties();

        if (!hasBeenChanged(entity)) {
            LOG.warn(entity + " has not been changed but the caller thinks it should have changed properties.");
            return false;
        }

        if (!changedProperties.containsKey(entity.getId())) {
            return false;
        }

        return changedProperties.get(entity.getId()).containsKey(key);
    }

    @Override
    public Map<String, Change<Object>> changedProperties(T entity) {
        initializeProperties();

        if (!hasBeenChanged(entity)) {
            LOG.warn(entity + " has not been changed but the caller thinks it should have changed properties.");
            return Collections.emptyMap();
        }

        if (!changedProperties.containsKey(entity.getId())) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(changedProperties.get(entity.getId()));
    }

    private void initializeProperties() {
        if (createdProperties != null) {
            assert changedProperties != null;
            assert deletedProperties != null;
            assert deletedEntityProperties != null;

            return;
        }

        //initializeCreated(); // - called by initializeChanged()
        //initializeDeleted(); // - called by initializeChanged()
        initializeChanged();

        createdProperties = new HashMap<>();
        deletedProperties = new HashMap<>();
        changedProperties = new HashMap<>();
        deletedEntityProperties = new HashMap<>();

        for (PropertyEntry<T> propertyEntry : assignedProperties()) {
            T entity = propertyEntry.entity();

            if (hasBeenCreated(entity)) {
                continue;
            }

            if (hasNotActuallyChanged(propertyEntry)) {
                continue;
            }

            if (propertyEntry.previouslyCommittedValue() == null) {
                if (!createdProperties.containsKey(entity.getId())) {
                    createdProperties.put(entity.getId(), new HashMap<String, Object>());
                }
                createdProperties.get(entity.getId()).put(propertyEntry.key(), propertyEntry.value());
            } else {
                if (!changedProperties.containsKey(entity.getId())) {
                    changedProperties.put(entity.getId(), new HashMap<String, Change<Object>>());
                }
                changedProperties.get(entity.getId()).put(propertyEntry.key(), new Change<>(propertyEntry.previouslyCommittedValue(), propertyEntry.value()));
            }
        }

        for (PropertyEntry<T> propertyEntry : removedProperties()) {
            T entity = propertyEntry.entity();

            if (deleted.containsKey(entity.getId())) {
                if (!deletedEntityProperties.containsKey(entity.getId())) {
                    deletedEntityProperties.put(entity.getId(), new HashMap<String, Object>());
                }
                deletedEntityProperties.get(entity.getId()).put(propertyEntry.key(), propertyEntry.previouslyCommittedValue());
                continue;
            }

            if (!changedContainsKey(entity)) {
                throw new IllegalStateException(entity + " seems to have not been deleted or changed, this is a bug");
            }

            assert changedContainsKey(entity);

            if (!deletedProperties.containsKey(entity.getId())) {
                deletedProperties.put(entity.getId(), new HashMap<String, Object>());
            }

            deletedProperties.get(entity.getId()).put(propertyEntry.key(), propertyEntry.previouslyCommittedValue());
        }
    }

    private boolean hasNotActuallyChanged(PropertyEntry<T> propertyEntry) {
        return propertyEntry.previouslyCommittedValue() != null && propertyEntry.previouslyCommittedValue().equals(propertyEntry.value());
    }
}
