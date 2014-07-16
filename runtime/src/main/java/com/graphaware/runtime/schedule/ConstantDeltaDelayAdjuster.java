package com.graphaware.runtime.schedule;

/**
 * Simple implementation of {@link DelayAdjuster} that makes adjustments of a constant size depending on the activity delta and
 * threshold values.
 */
public class ConstantDeltaDelayAdjuster implements DelayAdjuster {

	private final int adjustmentAmount;

	/**
	 * Constructs a new {@link ConstantDeltaDelayAdjuster} that lengthens or shortens the delay by the given amount.
	 *
	 * @param adjustmentAmountMillis The number of milliseconds by which to adjust the current delay
	 */
	public ConstantDeltaDelayAdjuster(int adjustmentAmountMillis) {
		adjustmentAmount = adjustmentAmountMillis;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation adjusts the delay by a predefined amount based on whether or not the difference is above or below
	 * the given threshold.
	 * </p>
	 */
	@Override
	public long determineNextDelay(long currentDelay, int txCountDifference, int threshold) {
		if (txCountDifference > threshold) {
			// had a lot of transactions since last time so back off a bit
			return currentDelay + adjustmentAmount;
		}
		// no significant increase so shorten amount
		return Math.max(currentDelay - adjustmentAmount, 0);
	}

}
