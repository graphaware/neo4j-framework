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

package com.graphaware.runtime.schedule;

/**
 * Algorithmically adjusts a scheduling time delay depending on certain parameters.
 */
public interface DelayAdjuster {

    /**
     * Determines the absolute number of milliseconds that should be used as the next timing delay for scheduling a module
     * invocation based on the given arguments.
     *
     * @param currentDelay     The currently-used delay length in milliseconds - i.e., the base value to adjust.
     * @param lastTaskDuration The number of milliseconds taken to execute the previously-scheduled task.
     * @param load             The load on the database in tx/s.
     * @return The absolute number of milliseconds that should be used for the next scheduling delay.
     */
    long determineNextDelay(long currentDelay, long lastTaskDuration, long load);
}
