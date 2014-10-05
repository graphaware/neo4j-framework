/*
 * Copyright (c) 2013 GraphAware
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

package com.graphaware.runtime.config;

import com.graphaware.runtime.schedule.TimingStrategy;
import com.graphaware.writer.DatabaseWriter;

/**
 * Base-class for {@link RuntimeConfiguration} implementations.
 */
public abstract class BaseRuntimeConfiguration implements RuntimeConfiguration {

    private final TimingStrategy timingStrategy;
    private final DatabaseWriter databaseWriter;

    protected BaseRuntimeConfiguration(TimingStrategy timingStrategy, DatabaseWriter databaseWriter) {
        this.timingStrategy = timingStrategy;
        this.databaseWriter = databaseWriter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimingStrategy getTimingStrategy() {
        return timingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseWriter getDatabaseWriter() {
        return databaseWriter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createPrefix(String id) {
        return GA_PREFIX + id + "_";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseRuntimeConfiguration that = (BaseRuntimeConfiguration) o;

        if (!databaseWriter.equals(that.databaseWriter)) return false;
        if (!timingStrategy.equals(that.timingStrategy)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = timingStrategy.hashCode();
        result = 31 * result + databaseWriter.hashCode();
        return result;
    }
}
