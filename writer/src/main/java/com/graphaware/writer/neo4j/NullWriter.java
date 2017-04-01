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

package com.graphaware.writer.neo4j;

import java.util.concurrent.Callable;

/**
 * A {@link Neo4jWriter} that throws an {@link UnsupportedOperationException} any time it is used for writing. Its
 * purpose is to serve as a placeholder for places where the use of {@link Neo4jWriter} does not make sense, e.g.
 * when using {@link org.neo4j.unsafe.batchinsert.BatchInserter}s. Singleton.
 */
public final class NullWriter implements Neo4jWriter {

    private static final NullWriter INSTANCE = new NullWriter();

    /**
     * Get an instance of this writer.
     *
     * @return instance.
     */
    public static NullWriter getInstance() {
        return INSTANCE;
    }

    private NullWriter() {
    }

    @Override
    public void start() {
        //no-op
    }

    @Override
    public void stop() {
        //no-op
    }

    @Override
    public void write(Runnable task) {
        throwException();
    }

    @Override
    public void write(Runnable task, String id) {
        throwException();
    }

    @Override
    public <T> T write(Callable<T> task, String id, int waitMillis) {
        throwException();
        return null;
    }

    private void throwException() {
        throw new UnsupportedOperationException("NullWriter should not be used for writing to the database. Are you using it in batch inserter mode?");
    }
}
