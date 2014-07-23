package com.graphaware.runtime.config;

/**
 * Interface to access the settings used to configure timer-driven components of the runtime, such as the task scheduler and
 * timing strategies.  The scheduling of timer-driven modules, in particular, is driven by this configuration.
 */
public interface ScheduleConfiguration {

	/**
	 * Retrieves the default delay to wait between scheduled task execution in milliseconds.
	 *
	 * @return The default delay before scheduling the next task.
	 */
	long defaultDelayMillis();

	/**
	 * Retrieves the shortest permitted time delay to wait between scheduled task execution in milliseconds.
	 *
	 * @return The minimum amount of time to wait between scheduled task execution.
	 */
	long minimumDelayMillis();

	/**
	 * Retrieves the longest permitted time delay to wait between scheduled task execution in milliseconds.
	 *
	 * @return The maximum amount of time to wait between scheduled task execution.
	 */
	long maximumDelayMillis();

	/**
	 * Retrieves a value used to control what constitutes a busy period of activity for the database service.
	 * <p>
	 * The semantics of the resultant value depends entirely on how its used and what the database activity metric is.
	 * Typically, it's a relative measure of transaction throughput, where a higher value represents a busier period of database
	 * activity.  The timer-driven module scheduling mechanism should use this setting to determine whether or not the database
	 * is particularly busy and thus change the scheduling delay accordingly.
	 * </p>
	 *
	 * @return A threshold against which a measure of the current database activity level is compared to determine whether or
	 *         not the current period is busy.
	 */
	int databaseActivityThreshold();

}
