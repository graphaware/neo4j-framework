/*
 * Copyright (c) 2013-2018 GraphAware
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

package com.graphaware.tx.event.improved.entity.snapshot;

import com.graphaware.common.wrapper.BaseEntityWrapper;
import com.graphaware.tx.event.improved.data.EntityTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Entity;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

/**
 * A decorator of a {@link org.neo4j.graphdb.Entity} that represents a snapshot of a {@link org.neo4j.graphdb.Entity} before a
 * transaction has started. It consults {@link com.graphaware.tx.event.improved.data.EntityTransactionData} wrapped in {@link com.graphaware.tx.event.improved.data.TransactionDataContainer},
 * before returning information about contained properties in order to provide these as they were before the transactions started.
 * Mutations are preformed as expected but only in the case that the mutated {@link org.neo4j.graphdb.Entity} has not been deleted
 * in the transaction. If it has been deleted, an exception is thrown upon mutation.
 *
 * @param <T> type of the wrapped entity.
 */
public abstract class EntitySnapshot<T extends Entity> extends BaseEntityWrapper<T> implements Entity {
    private static final Log LOG = LoggerFactory.getLogger(EntitySnapshot.class);

    protected final T wrapped;
    protected final TransactionDataContainer transactionDataContainer;

    /**
     * Construct a new snapshot.
     *
     * @param wrapped                  entity.
     * @param transactionDataContainer transaction data container.
     */
    protected EntitySnapshot(T wrapped, TransactionDataContainer transactionDataContainer) {
        this.wrapped = wrapped;
        this.transactionDataContainer = transactionDataContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getWrapped() {
        return wrapped;
    }

    /**
     * Get data about the transaction relevant for the subclass.
     *
     * @return transaction data.
     */
    protected abstract EntityTransactionData<T> transactionData();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        if (transactionData().hasBeenDeleted(wrapped)) {
            return transactionData().propertiesOfDeletedEntity(wrapped).containsKey(key);
        }

        if (transactionData().hasBeenChanged(wrapped)) {
            if (transactionData().hasPropertyBeenCreated(wrapped, key)) {
                return false;  //has been created in the transaction = wasn't there before
            }
            if (transactionData().hasPropertyBeenDeleted(wrapped, key)) {
                return true;   //has been deleted in the transaction = was there before
            }
        }

        //at this point, we know that either of the following is the case:
        //- the entity has not been changed by the tx
        //- the entity has been changed and the property we're looking for has not
        //- the entity has been changed and the property we're looking for has been changed
        // In either case, we can find out whether it was there by delegating to the wrapped one (its current version).

        return super.hasProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key) {
        if (!hasProperty(key)) {
            throw new NotFoundException("Snapshot of " + wrapped.toString() + " did not have a property with key " + key);
        }

        if (transactionData().hasBeenDeleted(wrapped)) {
            return transactionData().propertiesOfDeletedEntity(wrapped).get(key);
        }

        if (transactionData().hasBeenChanged(wrapped)) {
            if (transactionData().hasPropertyBeenChanged(wrapped, key)) {
                return transactionData().changedProperties(wrapped).get(key).getPrevious();
            }

            if (transactionData().hasPropertyBeenDeleted(wrapped, key)) {
                return transactionData().deletedProperties(wrapped).get(key);
            }
        }

        return super.getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        checkCanBeMutated();
        super.setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        checkCanBeMutated();
        return super.removeProperty(key);
    }

    /**
     * Check whether this entity has not been deleted and can thus be mutated.
     * In case the entity can be mutated, nothing happens. In the opposite case, an {@link IllegalStateException} exception is thrown.
     */
    protected void checkCanBeMutated() {
        if (transactionData().hasBeenDeleted(wrapped)) {
            LOG.error("Deleted entity " + wrapped + " should not be mutated.");
            throw new IllegalStateException("Deleted entity " + wrapped + " should not be mutated.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        if (transactionData().hasBeenDeleted(wrapped)) {
            return transactionData().propertiesOfDeletedEntity(wrapped).keySet();
        }

        if (!transactionData().hasBeenChanged(wrapped)) {
            return super.getPropertyKeys();
        }

        Collection<String> result = new HashSet<>();

        for (String key : super.getPropertyKeys()) {
            if (!transactionData().hasPropertyBeenCreated(wrapped, key)) {
                result.add(key);
            }
        }

        result.addAll(transactionData().deletedProperties(wrapped).keySet());

        return result;
    }
}
