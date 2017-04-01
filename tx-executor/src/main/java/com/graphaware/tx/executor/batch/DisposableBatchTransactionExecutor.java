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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link BatchTransactionExecutor} which expects the {@link #execute()} method to only ever be called at most once.
 * After that, it must be discarded.
 */
public abstract class DisposableBatchTransactionExecutor implements BatchTransactionExecutor {

    private final AtomicBoolean alreadyExecuted = new AtomicBoolean(false);

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute() {
        if (alreadyExecuted.compareAndSet(false, true)) {
            doExecute();
        } else {
            throw new IllegalStateException("DisposableBatchExecutor must only ever be executed once!");
        }
    }

    /**
     * Execute work in batches.
     */
    protected abstract void doExecute();
}
