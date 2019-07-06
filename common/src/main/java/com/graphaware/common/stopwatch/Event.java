/*
 * Copyright (c) 2013-2019 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.stopwatch;

import java.util.ArrayList;
import java.util.List;

public class Event {

    private Long start;

    private List<Period> periods = new ArrayList<>();

    private boolean isStopped = false;

    public Event() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        periods.add(new Period(getLastPeriodTime(), System.currentTimeMillis()));
    }

    public Long getStartTime() {
        return start;
    }

    public Long duration() {
        return getLastPeriodTime() - start;
    }

    public void addPeriod() {
        periods.add(new Period(getLastPeriodTime(), System.currentTimeMillis()));
    }

    public List<Period> getPeriods() {
        return periods;
    }

    private Long getLastPeriodTime() {
        return periods.size() == 0 ? start : periods.get(periods.size()-1).endTime();
    }
}
