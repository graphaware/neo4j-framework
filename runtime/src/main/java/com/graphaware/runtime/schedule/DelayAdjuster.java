package com.graphaware.runtime.schedule;

/**
 * Algorithmically adjusts a scheduling time delay depending on certain parameters.
 */
public interface DelayAdjuster {

    /**
     * Determines the absolute number of milliseconds that should be used as the next timing delay for scheduling a module
     * invocation based on the given arguments.
     *
     * @param currentDelay     The currently-used delay length in milliseconds - i.e., the base value to adjust.
     * @param lastTaskDuration The number of milliseconds taken to execute the previously-scheduled task.
     * @param load             The load on the database in tx/s.
     * @return The absolute number of milliseconds that should be used for the next scheduling delay.
     */
    long determineNextDelay(long currentDelay, long lastTaskDuration, long load);
}
