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

package com.graphaware.writer;

/**
 * A writer that writes to a system, which can be a third-party application, or Neo4j itself.
 * <p/>
 * Implementations can choose how they write to the system, but must make sure that tasks that are submitted to it
 * run within the context of a transaction.
 */
public interface Writer {

    /**
     * Start the writer.
     */
    void start();

    /**
     * Stop the writer.
     */
    void stop();
}
