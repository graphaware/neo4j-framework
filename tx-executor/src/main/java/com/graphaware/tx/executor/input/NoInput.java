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

import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;

/**
 * Dummy generated input that is intended for {@link BatchTransactionExecutor}s that use {@link UnitOfWork} but generate
 * their own input. Thus the only thing this class does is signal when enough units of work have been executed by returning
 * {@code false} from {@link #hasNext()}.
 */
public final class NoInput extends GeneratedInput<NullItem> {

    public NoInput(int numberOfItems) {
        super(numberOfItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NullItem generate() {
        return NullItem.getInstance();
    }
}
