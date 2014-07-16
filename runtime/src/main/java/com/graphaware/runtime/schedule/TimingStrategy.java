package com.graphaware.runtime.schedule;

/**
 * A strategy for timing scheduled tasks.
 */
public interface TimingStrategy {

    /**
     * Get the delay until the timer fires again.
     *
     * @param lastTaskDurationNanos The total time it took to execute the last task in nanoseconds. -1 if unknown.
     * @return The next delay in <b>milliseconds</b>.
     */
    long nextDelay(long lastTaskDurationNanos);

}
