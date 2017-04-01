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

package com.graphaware.tx.executor.batch;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A unit of work to be executed as a part of batch execution by {@link BatchTransactionExecutor}.
 *
 * @param <T> type of the input/parameter for the unit of work.
 */
public interface UnitOfWork<T> {

    /**
     * Execute the unit of work.
     *
     * @param database    against which to execute the work.
     * @param input       to the unit of work.
     * @param batchNumber current batch number.
     * @param stepNumber  current step number.
     */
    void execute(GraphDatabaseService database, T input, int batchNumber, int stepNumber);
}
