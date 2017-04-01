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

import com.graphaware.writer.Writer;

import java.util.Collection;

/**
 * A {@link Writer} to a third-party system.
 */
public interface ThirdPartyWriter extends Writer {

    /**
     * Write an operation.
     *
     * @param operation to write. Must not be <code>null</code>.
     * @param id        for logging purposes. Must not be <code>null</code>.
     */
    void write(WriteOperation<?> operation, String id);

    /**
     * Write a collection of operations.
     *
     * @param operations to write. Must not be <code>null</code>.
     * @param id         for logging purposes. Must not be <code>null</code>.
     */
    void write(Collection<WriteOperation<?>> operations, String id);

}
