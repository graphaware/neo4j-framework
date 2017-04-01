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

package com.graphaware.tx.executor.input;

import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.PrefetchingIterator;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;

/**
 * An {@link Iterable}, items of which are retrieved from the database in batches. Intended to be used as
 * input to implementations of {@link BatchTransactionExecutor}.
 *
 * @param <T> type of fetched input.
 */
public class TransactionalInput<T> extends PrefetchingIterator<T> implements Iterable<T>, Iterator<T> {
    private static final Log LOG = LoggerFactory.getLogger(TransactionalInput.class);

    private final GraphDatabaseService database;
    private final TransactionCallback<Iterable<T>> callback;
    private Iterator<T> iterator;
    private volatile int count = 0;
    private volatile Transaction tx;
    private final int batchSize;

    /**
     * Construct a new input.
     *
     * @param database  from which to fetch input, must not be <code>null</code>.
     * @param batchSize size of batches in which input if fetched. Must be positive.
     * @param callback  which actually retrieves an iterable from the database.
     */
    public TransactionalInput(GraphDatabaseService database, int batchSize, TransactionCallback<Iterable<T>> callback) {
        Objects.requireNonNull(database);
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize argument must be greater than zero");
        }
        Objects.requireNonNull(callback);

        this.database = database;
        this.callback = callback;
        this.batchSize = batchSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized T fetchNextOrNull() {
        beginTxIfNeeded();
        createIteratorIfNeeded();

        T next = null;
        if (iterator.hasNext()) {
            next = iterator.next();
        }

        if (next == null) {
            closeTx();
            return null;
        }

        int i = ++count % batchSize;
        if (i == 0) {
            closeTx();
        }

        return next;
    }

    private void createIteratorIfNeeded() {
        if (iterator == null) {
            try {
                iterator = callback.doInTransaction(database).iterator();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void beginTxIfNeeded() {
        if (tx == null) {
            tx = database.beginTx();
        }
    }

    private void closeTx() {
        if (tx == null) {
            return;
        }

        try {
            tx.success();
        } finally {
            tx.close();
            tx = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
