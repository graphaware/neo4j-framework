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
 * Strategy for dealing with exceptions during transaction execution.
 * <p/>
 * By exception, any {@link RuntimeException} is meant, i.e. both exception in the execution logic
 * (e.g. {@link IndexOutOfBoundsException}) and database exception (e.g. {@link org.neo4j.graphdb.TransactionFailureException}).
 */
public interface ExceptionHandlingStrategy {

    /**
     * Handle the exception. Handling could mean logging it, re-throwing it, or anything else plus combinations thereof.
     *
     * @param e to be handled.
     */
    void handleException(RuntimeException e);
}
