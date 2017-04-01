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

import com.graphaware.common.util.BoundedConcurrentStack;
import com.graphaware.common.util.Pair;
import com.graphaware.runtime.schedule.TimingStrategy;

import java.util.Iterator;

/**
 * Computes the average value per second of an ever-increasing value over the last configurable number samples or
 * configurable time in milliseconds, whichever is smaller.
 */
public class RunningWindowAverage {

    private final BoundedConcurrentStack<Pair<Long, Long>> timesAndValues;
    private final int maxTime;

    /**
     * Construct a new instance.
     *
     * @param maxSamples maximum number of samples kept in the running window.
     * @param maxTime    maximum amount of time span of the window.
     */
    public RunningWindowAverage(int maxSamples, int maxTime) {
        this.timesAndValues = new BoundedConcurrentStack<>(maxSamples);
        this.maxTime = maxTime;
    }

    /**
     * Take a sample.
     *
     * @param time  at which the value was taken.
     * @param value sample value.
     */
    public void sample(long time, long value) {
        timesAndValues.push(new Pair<>(time, value));
    }

    /**
     * Get average the ever-increasing value as the difference between the latest sample and some sample in the past,
     * divided by the two samples' time difference. The sample in the past is either maxSamples in the past,
     * or maxTime milliseconds in the past, whichever means a smaller interval.
     *
     * @return average of the value as described, rounded down to the nearest integer.
     */
    public long getAverage() {
        if (timesAndValues.isEmpty()) {
            return TimingStrategy.UNKNOWN;
        }

        Iterator<Pair<Long, Long>> iterator = timesAndValues.iterator();
        Pair<Long, Long> latest = iterator.next();
        long latestTime = latest.first();
        long latestValue = latest.second();

        if (!iterator.hasNext()) {
            return TimingStrategy.UNKNOWN;
        }

        long pastTime = 0;
        long pastValue = 0;

        while (iterator.hasNext()) {
            Pair<Long, Long> next = iterator.next();

            if (latestTime - next.first() > maxTime) {
                break;
            }

            pastTime = next.first();
            pastValue = next.second();
        }

        int period = Long.valueOf(latestTime - pastTime).intValue();

        if (period < 1) {
            return TimingStrategy.UNKNOWN;
        }

        return ((latestValue - pastValue) * 1000) / period;
    }
}
