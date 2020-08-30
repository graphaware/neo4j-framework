/*
 * Copyright (c) 2013-2020 GraphAware
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeriodTest {

    @Test
    public void shouldReturnStartAndEndTime() {
        Long t = System.currentTimeMillis();
        Long t2 = System.currentTimeMillis();
        Period period = new Period(t, t2);
        assertEquals(t, period.startTime());
        assertEquals(t2, period.endTime());
    }

    @Test
    public void shouldReturnDuration() {
        Long t = System.currentTimeMillis();
        Long t2 = t + 1000;
        Period period = new Period(t, t2);
        assertEquals(1000, period.duration(), 0L);
    }

}
