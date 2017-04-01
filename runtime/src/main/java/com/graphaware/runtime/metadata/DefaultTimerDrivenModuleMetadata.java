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

package com.graphaware.runtime.metadata;

/**
 * Default production implementation of {@link com.graphaware.runtime.metadata.TimerDrivenModuleMetadata}.
 */
public class DefaultTimerDrivenModuleMetadata implements TimerDrivenModuleMetadata {

    private final TimerDrivenModuleContext context;

    /**
     * Construct new metadata.
     *
     * @param context module context held by the metadata. Can be null in case it is unknown (first run).
     */
    public DefaultTimerDrivenModuleMetadata(TimerDrivenModuleContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleContext<?> lastContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultTimerDrivenModuleMetadata that = (DefaultTimerDrivenModuleMetadata) o;

        if (context != null ? !context.equals(that.context) : that.context != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return context != null ? context.hashCode() : 0;
    }
}
