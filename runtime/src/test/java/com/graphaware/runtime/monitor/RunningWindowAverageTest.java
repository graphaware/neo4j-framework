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

package com.graphaware.runtime.monitor;

import com.graphaware.runtime.schedule.TimingStrategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link RunningWindowAverage}.
 */
public class RunningWindowAverageTest {

    @Test
    public void shouldReturnCorrectValues() {
        RunningWindowAverage average = new RunningWindowAverage(5, 2000);
        assertEquals(TimingStrategy.UNKNOWN, average.getAverage());

        average.sample(10_000L, 10);
        assertEquals(TimingStrategy.UNKNOWN, average.getAverage());

        average.sample(11_000L, 20);
        assertEquals(10, average.getAverage());

        average.sample(12_000L, 40);
        assertEquals(15, average.getAverage());

        average.sample(12_500L, 45);
        assertEquals(16, average.getAverage()); // 25/1.5

        average.sample(13_000L, 50);
        assertEquals(15, average.getAverage()); // 30/2

        average.sample(13_300L, 70);
        assertEquals(23, average.getAverage()); // 30/1.3

        average.sample(13_600L, 75);
        average.sample(13_800L, 80);
        average.sample(14_000L, 82);
        assertEquals(32, average.getAverage()); // 32/1 (5 samples max)
    }
}
