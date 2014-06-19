package com.graphaware.runtime.timer;

/**
 * A strategy for timing scheduled tasks.
 */
public interface TimingStrategy {

    /**
     * Get the delay until the timer fires again.
     *
     * @param lastTaskDuration the total time it took to execute the last task in microseconds. -1 if unknown.
     * @return delay in ms.
     */
    long nextDelay(long lastTaskDuration);
}
