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
 * Base-class for {@link TimerDrivenModuleContext} implementations.
 *
 * @param <T> type of the position representation.
 */
public abstract class BaseTimerDrivenModuleContext<T> implements TimerDrivenModuleContext<T> {

    private final long earliestNextCall;

    /**
     * Create a new context indicating the module should be called ASAP.
     */
    public BaseTimerDrivenModuleContext() {
        this(ASAP);
    }

    /**
     * Create a new context indicating when is the earliest time the module should be called.
     *
     * @param earliestNextCall in ms since 1/1/1970
     */
    public BaseTimerDrivenModuleContext(long earliestNextCall) {
        this.earliestNextCall = earliestNextCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long earliestNextCall() {
        return earliestNextCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTimerDrivenModuleContext that = (BaseTimerDrivenModuleContext) o;

        if (earliestNextCall != that.earliestNextCall) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (earliestNextCall ^ (earliestNextCall >>> 32));
    }
}
