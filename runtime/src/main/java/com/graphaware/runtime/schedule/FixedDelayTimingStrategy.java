package com.graphaware.runtime.schedule;

import com.graphaware.runtime.config.RuntimeConfiguration;

import static com.graphaware.runtime.config.RuntimeConfiguration.*;

/**
 * {@link TimingStrategy} that causes tasks to be scheduled in regular intervals.
 */
public class FixedDelayTimingStrategy implements TimingStrategy {

    private final long delay;

    /**
     * Construct a new strategy.
     *
     * @param delay fixed delay in ms.
     */
    public FixedDelayTimingStrategy(long delay) {
        this.delay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long nextDelay(long lastTaskDuration) {
        if (lastTaskDuration == -2) {
            return INITIAL_TIMER_DELAY;
        }
        return delay;
    }
}
