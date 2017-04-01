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

import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.input.NoInput;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A {@link BatchTransactionExecutor} that executes a specified number of {@link UnitOfWork} in batches.
 * These units need no input.
 */
public class NoInputBatchTransactionExecutor extends IterableInputBatchTransactionExecutor<NullItem> implements BatchTransactionExecutor {

    /**
     * Construct a new batch executor.
     *
     * @param database   against which to execute batched queries.
     * @param batchSize  how many {@link UnitOfWork} are in a single batch.
     * @param noSteps    how many {@link UnitOfWork} should be executed in total.
     * @param unitOfWork a unit of work definition. Must be thread-safe.
     */
    public NoInputBatchTransactionExecutor(GraphDatabaseService database, int batchSize, int noSteps, UnitOfWork<NullItem> unitOfWork) {
        super(database, batchSize, new NoInput(noSteps), unitOfWork);
    }
}
