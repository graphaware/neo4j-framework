package com.graphaware.runtime.schedule;

/**
 * Algorithmically adjusts a scheduling time delay depending on certain parameters.
 */
public interface DelayAdjuster {

	/**
	 * Determines the absolute number of milliseconds that should be used as the next timing delay for scheduling a module
	 * invocation based on the given arguments.
	 *
	 * @param currentDelay The currently-used delay length in milliseconds - i.e., the base value to adjust.
	 * @param activityDelta A measure of how much activity has occurred in the database system during a recent time frame.
	 * @param threshold A threshold against which the <em>activityDelta</em> is compared to determine whether the current levels
	 *        of activity merit a reduction or an increase in the delay.
	 * @return The absolute number of milliseconds that should be used for the next scheduling delay.
	 */
	long determineNextDelay(long currentDelay, int activityDelta, int threshold);

}
