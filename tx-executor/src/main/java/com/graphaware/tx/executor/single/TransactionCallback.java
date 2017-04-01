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

/**
 * A specification of work to be executed within a transaction.
 *
 * @param <T> type of what is returned in case of a successful TX execution.
 */
public interface TransactionCallback<T> {

    /**
     * Perform the work. This method is guaranteed to run in the context of a transaction.
     *
     * @param database on which to perform work and against which the transaction is running.
     * @return result of the transaction, can be null in case of a failure (or when no result is expected, in which case
     *         the generic type of the implementation should be {@link Void}.
     */
    T doInTransaction(final GraphDatabaseService database) throws Exception;
}
