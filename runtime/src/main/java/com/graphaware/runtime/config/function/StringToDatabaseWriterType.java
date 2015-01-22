/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.config.function;

import com.graphaware.runtime.write.DatabaseWriterType;
import org.neo4j.helpers.Function;

/**
 * A {@link org.neo4j.helpers.Function} that converts String to {@link DatabaseWriterType}. Singleton.
 */
public final class StringToDatabaseWriterType implements Function<String, DatabaseWriterType> {

    public static final String DEFAULT = "default";
    public static final String SINGLE_THREADED = "single";
    public static final String BATCH = "batch";

    private static StringToDatabaseWriterType INSTANCE = new StringToDatabaseWriterType();

    public static StringToDatabaseWriterType getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseWriterType apply(String s) {
        if (s.equalsIgnoreCase(DEFAULT)) {
            return DatabaseWriterType.DEFAULT;
        }

        if (s.equalsIgnoreCase(SINGLE_THREADED)) {
            return DatabaseWriterType.SINGLE_THREADED;
        }

        if (s.equalsIgnoreCase(BATCH)) {
            return DatabaseWriterType.BATCH;
        }

        throw new IllegalStateException("Unknown database writer: " + s);
    }
}
