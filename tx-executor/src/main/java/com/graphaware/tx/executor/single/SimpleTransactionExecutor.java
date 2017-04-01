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

package com.graphaware.tx.executor.single;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

/**
 * A convenience class that executes work in the context of a {@link org.neo4j.graphdb.Transaction}.
 * <p/>
 * This class is thread-safe.
 */
public class SimpleTransactionExecutor implements TransactionExecutor {
    private static final Log LOG = LoggerFactory.getLogger(SimpleTransactionExecutor.class);

    private final GraphDatabaseService database;

    /**
     * Construct a new executor.
     *
     * @param database against which transactions will be executed.
     */
    public SimpleTransactionExecutor(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        return executeInTransaction(callback, RethrowException.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T executeInTransaction(TransactionCallback<T> callback, ExceptionHandlingStrategy exceptionHandlingStrategy) {
        T result = null;
        try {
            result = doExecuteInTransaction(callback);
        } catch (RuntimeException e) {
            exceptionHandlingStrategy.handleException(e);
        }
        return result;
    }

    private <T> T doExecuteInTransaction(TransactionCallback<T> callback) {
        Transaction tx = database.beginTx();
        T result = null;
        try {
            result = callback.doInTransaction(database); //can throw a business exception
            tx.success();
        } catch (RuntimeException e) {
            LOG.warn("Runtime exception occurred during transaction execution.", e);
            tx.failure();
            throw e;
        } catch (Exception e) {
            LOG.warn("Checked exception occurred during transaction execution.", e);
            tx.failure();
            throw new RuntimeException(e);
        } finally {
            tx.close(); //can throw a DB exception
        }
        return result;
    }
}
