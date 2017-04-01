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

import com.graphaware.runtime.monitor.DatabaseLoadMonitor;
import com.graphaware.runtime.monitor.RunningWindowAverage;
import com.graphaware.runtime.monitor.StartedTxBasedLoadMonitor;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Implementation of {@link TimingStrategy} that pays attention to the current level of activity in the database, i.e.
 * the number of started transactions, in order to decide how long to wait before scheduling the next task.
 */
public class AdaptiveTimingStrategy implements TimingStrategy {

    private final long delta;
    private final long defaultDelay;
    private final long minDelay;
    private final long maxDelay;
    private final long busyThreshold;
    private final int maxSamples;
    private final int maxTime;

    private DelayAdjuster delayAdjuster;
    private DatabaseLoadMonitor loadMonitor;

    private long previousDelay = UNKNOWN;

    /**
     * Create a new instance of this strategy with default configuration, which is:
     * <ul>
     * <li>delta = 100ms</li>
     * <li>default delay = 2s</li>
     * <li>minimum delay = 5ms</li>
     * <li>maximum delay = 10s</li>
     * <li>busy threshold = 100</li>
     * <li>maximum samples = 200</li>
     * <li>maximum time = 2s</li>
     * </ul>
     *
     * @return instance of this strategy.
     */
    public static AdaptiveTimingStrategy defaultConfiguration() {
        return new AdaptiveTimingStrategy(100, 2_000, 5, 5_000, 100, 200, 2_000);
    }

    /**
     * Constructs a new instance of this strategy with the specified configuration settings.
     *
     * @param delta         The number of milliseconds by which to adjust the current delay.
     * @param defaultDelay  The number of milliseconds to return if there is not enough information to make a better decision.
     * @param minDelay      The lower limit to the delay that can be returned as the next delay.
     * @param maxDelay      The upper limit to the delay that can be returned as the next delay.
     * @param busyThreshold The number of transactions per second, above which the database is deemed
     *                      to be busy.
     * @param maxSamples    The maximum number of running window average samples. See {@link RunningWindowAverage}.
     * @param maxTime       The maximum amount of running window average time. See {@link RunningWindowAverage}.
     */
    private AdaptiveTimingStrategy(long delta, long defaultDelay, long minDelay, long maxDelay, long busyThreshold, int maxSamples, int maxTime) {
        this.delta = delta;
        this.defaultDelay = defaultDelay;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.busyThreshold = busyThreshold;
        this.maxSamples = maxSamples;
        this.maxTime = maxTime;
    }

    /**
     * Returns a copy of this {@link AdaptiveTimingStrategy} reconfigured to use the specified delta.
     *
     * @param delta The new delta in milliseconds.
     * @return A new {@link AdaptiveTimingStrategy}.
     */
    public AdaptiveTimingStrategy withDelta(long delta) {
        return new AdaptiveTimingStrategy(delta, this.defaultDelay, this.minDelay, this.maxDelay, this.busyThreshold, this.maxSamples, this.maxTime);
    }

    /**
     * Returns a copy of this {@link AdaptiveTimingStrategy} reconfigured to use the specified default timer-driven
     * module scheduling delay.
     *
     * @param defaultDelay The new default scheduling delay in milliseconds.
     * @return A new {@link AdaptiveTimingStrategy}.
     */
    public AdaptiveTimingStrategy withDefaultDelayMillis(long defaultDelay) {
        return new AdaptiveTimingStrategy(this.delta, defaultDelay, this.minDelay, this.maxDelay, this.busyThreshold, this.maxSamples, this.maxTime);
    }

    /**
     * Returns a copy of this {@link AdaptiveTimingStrategy} reconfigured to use the specified minimum delay between
     * timer-driven module invocations.
     *
     * @param minDelay The new minimum delay between timer-driven module invocations.
     * @return A new {@link AdaptiveTimingStrategy}.
     */
    public AdaptiveTimingStrategy withMinimumDelayMillis(long minDelay) {
        return new AdaptiveTimingStrategy(this.delta, this.defaultDelay, minDelay, this.maxDelay, this.busyThreshold, this.maxSamples, this.maxTime);
    }

    /**
     * Returns a copy of this {@link AdaptiveTimingStrategy} reconfigured to use the specified maximum delay between
     * timer-driven module invocations.
     *
     * @param maxDelay The new maximum delay between timer-driven module invocations.
     * @return A new {@link AdaptiveTimingStrategy}.
     */
    public AdaptiveTimingStrategy withMaximumDelayMillis(long maxDelay) {
        return new AdaptiveTimingStrategy(this.delta, this.defaultDelay, this.minDelay, maxDelay, this.busyThreshold, this.maxSamples, this.maxTime);
    }

    /**
     * Returns a copy of this {@link AdaptiveTimingStrategy} reconfigured to use the given busy threshold.
     *
     * @param busyThreshold The new busy threshold to use.
     * @return A new {@link AdaptiveTimingStrategy}.
     */
    public AdaptiveTimingStrategy withBusyThreshold(int busyThreshold) {
        return new AdaptiveTimingStrategy(this.delta, this.defaultDelay, this.minDelay, this.maxDelay, busyThreshold, this.maxSamples, this.maxTime);
    }

    /**
     * Returns a copy of this {@link AdaptiveTimingStrategy} reconfigured to use the maximum number of samples.
     *
     * @param maxSamples The new maximum number of samples to use.
     * @return A new {@link AdaptiveTimingStrategy}.
     */
    public AdaptiveTimingStrategy withMaxSamples(int maxSamples) {
        return new AdaptiveTimingStrategy(this.delta, this.defaultDelay, this.minDelay, this.maxDelay, this.busyThreshold, maxSamples, this.maxTime);
    }

    /**
     * Returns a copy of this {@link AdaptiveTimingStrategy} reconfigured to use the given maximum running average window time span.
     *
     * @param maxTime The new maximum time to use.
     * @return A new {@link AdaptiveTimingStrategy}.
     */
    public AdaptiveTimingStrategy withMaxTime(int maxTime) {
        return new AdaptiveTimingStrategy(this.delta, this.defaultDelay, this.minDelay, this.maxDelay, this.busyThreshold, this.maxSamples, maxTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        this.delayAdjuster = new ConstantDeltaDelayAdjuster(this.delta, this.defaultDelay, this.minDelay, this.maxDelay, this.busyThreshold);
        this.loadMonitor = new StartedTxBasedLoadMonitor(database, new RunningWindowAverage(this.maxSamples, this.maxTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextDelay(long lastTaskDuration) {
        if (delayAdjuster == null || loadMonitor == null) {
            throw new IllegalStateException("Initialization hasn't been performed, this is a bug.");
        }

        long newDelay = delayAdjuster.determineNextDelay(previousDelay, lastTaskDuration, loadMonitor.getLoad());

        previousDelay = newDelay;

        return newDelay;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdaptiveTimingStrategy that = (AdaptiveTimingStrategy) o;

        if (busyThreshold != that.busyThreshold) return false;
        if (defaultDelay != that.defaultDelay) return false;
        if (delta != that.delta) return false;
        if (maxDelay != that.maxDelay) return false;
        if (maxSamples != that.maxSamples) return false;
        if (maxTime != that.maxTime) return false;
        if (minDelay != that.minDelay) return false;

        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = (int) (delta ^ (delta >>> 32));
        result = 31 * result + (int) (defaultDelay ^ (defaultDelay >>> 32));
        result = 31 * result + (int) (minDelay ^ (minDelay >>> 32));
        result = 31 * result + (int) (maxDelay ^ (maxDelay >>> 32));
        result = 31 * result + (int) (busyThreshold ^ (busyThreshold >>> 32));
        result = 31 * result + maxSamples;
        result = 31 * result + maxTime;
        return result;
    }
}
