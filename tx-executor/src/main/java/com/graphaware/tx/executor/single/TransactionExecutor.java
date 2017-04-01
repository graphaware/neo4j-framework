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

/**
 * Implementations execute work in Neo4j {@link org.neo4j.graphdb.Transaction}s.
 */
public interface TransactionExecutor {

    /**
     * Execute work in a single transaction, using {@link RethrowException} as {@link ExceptionHandlingStrategy}.
     *
     * @param callback specifying the work to be executed.
     * @param <T>      type of execution result. If no result is expected, this should be {@link com.graphaware.tx.executor.NullItem}.
     *                 It could also be {@link Void} (but the former is generally preferred).
     * @return result of the execution.
     * @throws RuntimeException if any occurred during the transaction.
     */
    <T> T executeInTransaction(TransactionCallback<T> callback);

    /**
     * Execute work in a single transaction.
     *
     * @param callback                  specifying the work to be executed.
     * @param exceptionHandlingStrategy what to do in case an exception occurs. This could be either an exception thrown
     *                                  by the callback, or by the database.
     * @param <T>                       type of execution result. If no result is expected, this should be {@link com.graphaware.tx.executor.NullItem}.
     *                                  It could also be {@link Void} and always {@code null}, but then a successful
     *                                  execution and failed execution isn't distinguishable if {@link KeepCalmAndCarryOn}
     *                                  exception handling strategy is used.
     * @return result of the execution. Will be {@code null} in case a result is expected, but an exception occurred during the execution
     *         of the transaction, which isn't re-thrown by the exception handling strategy.
     */
    <T> T executeInTransaction(TransactionCallback<T> callback, ExceptionHandlingStrategy exceptionHandlingStrategy);
}
