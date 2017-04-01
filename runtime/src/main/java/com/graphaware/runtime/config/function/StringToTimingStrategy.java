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

package com.graphaware.runtime.config.function;

import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;

import java.util.function.Function;

/**
 * A {@link Function} that converts String to {@link TimingStrategy}. Singleton.
 * <p/>
 * Converts "fixed" to {@link FixedDelayTimingStrategy} and "adaptive" to {@link AdaptiveTimingStrategy}.
 */
public final class StringToTimingStrategy implements Function<String, TimingStrategy> {

    public static final String FIXED = "fixed";
    public static final String ADAPTIVE = "adaptive";

    private static StringToTimingStrategy INSTANCE = new StringToTimingStrategy();

    public static StringToTimingStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimingStrategy apply(String s) {
        if (s.equalsIgnoreCase(FIXED)) {
            return FixedDelayTimingStrategy.getInstance();
        }

        if (s.equalsIgnoreCase(ADAPTIVE)) {
            return AdaptiveTimingStrategy.defaultConfiguration();
        }

        throw new IllegalStateException("Unknown timing strategy: " + s);
    }
}
