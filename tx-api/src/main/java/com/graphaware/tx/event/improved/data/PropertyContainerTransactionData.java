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
import org.neo4j.graphdb.PropertyContainer;

import java.util.Collection;
import java.util.Map;

/**
 * An alternative API to {@link org.neo4j.graphdb.event.TransactionData} for {@link org.neo4j.graphdb.PropertyContainer}s.
 *
 * @param <T> type of the property container.
 */
public interface PropertyContainerTransactionData<T extends PropertyContainer> {

    /**
     * Check whether the given property container has been created in the transaction.
     *
     * @param container to check.
     * @return true iff the property container has been created.
     */
    boolean hasBeenCreated(T container);

    /**
     * Get all property containers created in the transaction.
     *
     * @return read-only collection of all created property containers.
     */
    Collection<T> getAllCreated();

    /**
     * Check whether the given property container has been deleted in the transaction.
     *
     * @param container to check.
     * @return true iff the property container has been deleted.
     */
    boolean hasBeenDeleted(T container);

    /**
     * Get a property container that has been deleted in this transaction as it was before the transaction started.
     *
     * @param container to get.
     * @return snapshot of the property container before the transaction started.
     * @throws IllegalArgumentException in case the given property container has not been deleted in the transaction.
     */
    T getDeleted(T container);

    /**
     * Get all property containers deleted in the transaction as they were before the transaction started.
     *
     * @return read-only collection of all deleted property containers as they were before the transaction started (snapshots).
     */
    Collection<T> getAllDeleted();

    /**
     * Check whether a property container has been changed in the transaction, i.e. if any of its properties have been changed.
     *
     * @param container to check.
     * @return true iff the property container has been changed.
     */
    boolean hasBeenChanged(T container);

    /**
     * Get a property container that has been changed in this transaction as it was before the transaction started and as it is now.
     *
     * @param container to get.
     * @return snapshot of the property container before the transaction started and the current state of the property container.
     * @throws IllegalArgumentException in case the given property container has not been changed in the transaction.
     */
    Change<T> getChanged(T container);

    /**
     * Get all property containers changed in the transaction.
     *
     * @return a read-only collection of all changed property containers as they were before the transaction started and as they are now.
     */
    Collection<Change<T>> getAllChanged();

    /**
     * Check whether a property has been created in the transaction.
     *
     * @param container to check.
     * @param key       of the property to check.
     * @return true iff the property has been created.
     */
    boolean hasPropertyBeenCreated(T container, String key);

    /**
     * Get properties created in the transaction.
     *
     * @param container for which to get created properties.
     * @return read-only properties created for the given container.
     */
    Map<String, Object> createdProperties(T container);

    /**
     * Check whether a property has been deleted in the transaction.
     *
     * @param container to check.
     * @param key       of the property to check.
     * @return true iff the property has been deleted.
     */
    boolean hasPropertyBeenDeleted(T container, String key);

    /**
     * Get properties deleted in the transaction.
     *
     * @param container for which to get deleted properties.
     * @return read-only properties deleted for the given container, where the value is the property value before the
     *         transaction started.
     */
    Map<String, Object> deletedProperties(T container);

    /**
     * Get properties of a deleted container.
     *
     * @param container deleted container.
     * @return read-only properties of the deleted container, where the value is the property value before the
     *         transaction started.
     */
    Map<String, Object> propertiesOfDeletedContainer(T container);

    /**
     * Check whether a property has been changed in the transaction.
     *
     * @param container to check.
     * @param key       of the property to check.
     * @return true iff the property has been changed.
     */
    boolean hasPropertyBeenChanged(T container, String key);

    /**
     * Get properties changed in the transaction.
     *
     * @param container for which to get changed properties.
     * @return read-only properties changed for the given container, where the value is the property value before and
     *         after the transaction started, respectively.
     */
    Map<String, Change<Object>> changedProperties(T container);
}
