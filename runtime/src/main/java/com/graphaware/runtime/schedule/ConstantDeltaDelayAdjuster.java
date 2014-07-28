package com.graphaware.runtime.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of {@link DelayAdjuster} that makes adjustments of a constant size depending on the activity
 * delta and threshold values.
 */
public class ConstantDeltaDelayAdjuster implements DelayAdjuster {
    private static final Logger LOG = LoggerFactory.getLogger(ConstantDeltaDelayAdjuster.class);

    private final long delta;
    private final long defaultDelay;
    private final long minDelay;
    private final long maxDelay;
    private final long busyThreshold;

    /**
     * Constructs a new {@link ConstantDeltaDelayAdjuster} that lengthens (iff the database is busy) or shortens the
     * delay by the given amount.
     *
     * @param delta         The number of milliseconds by which to adjust the current delay.
     * @param defaultDelay  The number of milliseconds to return if there is not enough information to make a better decision.
     * @param minDelay      The lower limit to the delay that can be returned as the next delay.
     * @param maxDelay      The upper limit to the delay that can be returned as the next delay.
     * @param busyThreshold the number of transactions per second, above which the database is deemed
     *                      to be busy.
     */
    public ConstantDeltaDelayAdjuster(long delta, long defaultDelay, long minDelay, long maxDelay, long busyThreshold) {
        this.delta = delta;
        this.defaultDelay = defaultDelay;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.busyThreshold = busyThreshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long determineNextDelay(long currentDelay, long lastTaskDuration, int load) {
        if (currentDelay < 0) {
            return defaultDelay;
        }

        if (load > busyThreshold) {
            // had a lot of transactions since last time so back off a bit
            long result = Math.min(currentDelay + delta, maxDelay);
            log(result, load);
            return result;
        }

        // no significant increase so shorten amount
        long result = Math.max(currentDelay - delta, minDelay);
        log(result, load);
        return result;
    }

    private void log(long result, long rate) {
        LOG.debug("Next delay updated to {} ms based on average load of {} tx/s", result, rate);
    }
}
