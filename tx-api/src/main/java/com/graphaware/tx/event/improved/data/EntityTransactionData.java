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

package com.graphaware.tx.event.improved.data;

import com.graphaware.common.util.Change;
import org.neo4j.graphdb.Entity;

import java.util.Collection;
import java.util.Map;

/**
 * An alternative API to {@link org.neo4j.graphdb.event.TransactionData} for {@link org.neo4j.graphdb.Entity}s.
 *
 * @param <T> type of the entity.
 */
public interface EntityTransactionData<T extends Entity> {

    /**
     * Check whether the given entity has been created in the transaction.
     *
     * @param entity to check.
     * @return true iff the entity has been created.
     */
    boolean hasBeenCreated(T entity);

    /**
     * Get all entities created in the transaction.
     *
     * @return read-only collection of all created entities.
     */
    Collection<T> getAllCreated();

    /**
     * Check whether the given entity has been deleted in the transaction.
     *
     * @param entity to check.
     * @return true iff the entity has been deleted.
     */
    boolean hasBeenDeleted(T entity);

    /**
     * Get a entity that has been deleted in this transaction as it was before the transaction started.
     *
     * @param entity to get.
     * @return snapshot of the entity before the transaction started.
     * @throws IllegalArgumentException in case the given entity has not been deleted in the transaction.
     */
    T getDeleted(T entity);

    /**
     * Get all entities deleted in the transaction as they were before the transaction started.
     *
     * @return read-only collection of all deleted entities as they were before the transaction started (snapshots).
     */
    Collection<T> getAllDeleted();

    /**
     * Check whether a entity has been changed in the transaction, i.e. if any of its properties have been changed.
     *
     * @param entity to check.
     * @return true iff the entity has been changed.
     */
    boolean hasBeenChanged(T entity);

    /**
     * Get a entity that has been changed in this transaction as it was before the transaction started and as it is now.
     *
     * @param entity to get.
     * @return snapshot of the entity before the transaction started and the current state of the entity.
     * @throws IllegalArgumentException in case the given entity has not been changed in the transaction.
     */
    Change<T> getChanged(T entity);

    /**
     * Get all entities changed in the transaction.
     *
     * @return a read-only collection of all changed entities as they were before the transaction started and as they are now.
     */
    Collection<Change<T>> getAllChanged();

    /**
     * Check whether a property has been created in the transaction.
     *
     * @param entity to check.
     * @param key       of the property to check.
     * @return true iff the property has been created.
     */
    boolean hasPropertyBeenCreated(T entity, String key);

    /**
     * Get properties created in the transaction.
     *
     * @param entity for which to get created properties.
     * @return read-only properties created for the given entity.
     */
    Map<String, Object> createdProperties(T entity);

    /**
     * Check whether a property has been deleted in the transaction.
     *
     * @param entity to check.
     * @param key       of the property to check.
     * @return true iff the property has been deleted.
     */
    boolean hasPropertyBeenDeleted(T entity, String key);

    /**
     * Get properties deleted in the transaction.
     *
     * @param entity for which to get deleted properties.
     * @return read-only properties deleted for the given entity, where the value is the property value before the
     *         transaction started.
     */
    Map<String, Object> deletedProperties(T entity);

    /**
     * Get properties of a deleted entity.
     *
     * @param entity deleted entity.
     * @return read-only properties of the deleted entity, where the value is the property value before the
     *         transaction started.
     */
    Map<String, Object> propertiesOfDeletedEntity(T entity);

    /**
     * Check whether a property has been changed in the transaction.
     *
     * @param entity to check.
     * @param key       of the property to check.
     * @return true iff the property has been changed.
     */
    boolean hasPropertyBeenChanged(T entity, String key);

    /**
     * Get properties changed in the transaction.
     *
     * @param entity for which to get changed properties.
     * @return read-only properties changed for the given entity, where the value is the property value before and
     *         after the transaction started, respectively.
     */
    Map<String, Change<Object>> changedProperties(T entity);
}
