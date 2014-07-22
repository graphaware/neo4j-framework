package com.graphaware.runtime.schedule;

/**
 * Simple implementation of {@link DelayAdjuster} that makes adjustments of a constant size depending on the activity delta and
 * threshold values.
 */
public class ConstantDeltaDelayAdjuster implements DelayAdjuster {

	private final long adjustmentAmount;
	private final long minDelay;
	private final long maxDelay;

	/**
	 * Constructs a new {@link ConstantDeltaDelayAdjuster} that lengthens or shortens the delay by the given amount.
	 *
	 * @param adjustmentAmountMillis The number of milliseconds by which to adjust the current delay.
	 * @param minDelay The lower limit to the delay that can be returned as the next delay
	 * @param maxDelay The upper limit to the delay that can be returned as the next delay
	 */
	public ConstantDeltaDelayAdjuster(long adjustmentAmountMillis, long minDelay, long maxDelay) {
		this.adjustmentAmount = adjustmentAmountMillis;
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation adjusts the delay by a predefined amount based on whether or not the difference is above or below the
	 * given threshold.
	 * </p>
	 *
	 * @param threshold The minimum difference in transaction count between invocations of this method that is considered to
	 *        constitute a busy period of database activity
	 */
	@Override
	public long determineNextDelay(long currentDelay, int txCountDifference, int threshold) {
		if (txCountDifference > threshold) {
			// had a lot of transactions since last time so back off a bit
			return Math.min(currentDelay + adjustmentAmount, maxDelay);
		}
		// no significant increase so shorten amount
		return Math.max(currentDelay - adjustmentAmount, minDelay);
	}

}
