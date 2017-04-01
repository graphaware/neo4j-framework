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

package com.graphaware.writer.thirdparty;

import static org.springframework.util.Assert.notNull;

/**
 * Abstract base-class for {@link WriteOperation} implementations.
 *
 * @param <T> type of the object that provides details about the operation.
 */
public abstract class BaseWriteOperation<T> implements WriteOperation<T> {

    private final T details;

    /**
     * Create the operation.
     *
     * @param details details about the operation. Must not be <code>null</code>.
     */
    protected BaseWriteOperation(T details) {
        notNull(details);

        this.details = details;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getDetails() {
        return details;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseWriteOperation<?> that = (BaseWriteOperation<?>) o;

        return details.equals(that.details);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return details.hashCode();
    }
}
