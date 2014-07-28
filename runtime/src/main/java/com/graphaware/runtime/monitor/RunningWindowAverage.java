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

    private final BoundedConcurrentStack<Pair<Long, Integer>> timesAndValues;
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
    public void sample(long time, int value) {
        timesAndValues.push(new Pair<>(time, value));
    }

    /**
     * Get average the ever-increasing value as the difference between the latest sample and some sample in the past,
     * divided by the two samples' time difference. The sample in the past is either maxSamples in the past,
     * or maxTime milliseconds in the past, whichever means a smaller interval.
     *
     * @return average of the value as described, rounded down to the nearest integer.
     */
    public int getAverage() {
        if (timesAndValues.isEmpty()) {
            return TimingStrategy.UNKNOWN;
        }

        Iterator<Pair<Long, Integer>> iterator = timesAndValues.iterator();
        Pair<Long, Integer> latest = iterator.next();
        long latestTime = latest.first();
        int latestValue = latest.second();

        if (!iterator.hasNext()) {
            return TimingStrategy.UNKNOWN;
        }

        long pastTime = 0;
        int pastValue = 0;

        while (iterator.hasNext()) {
            Pair<Long, Integer> next = iterator.next();

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
