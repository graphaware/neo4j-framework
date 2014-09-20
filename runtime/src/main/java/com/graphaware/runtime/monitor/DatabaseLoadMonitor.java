package com.graphaware.runtime.monitor;

/**
 * A component monitoring the current database load.
 */
public interface DatabaseLoadMonitor {

    /**
     * Get the current load of the database in transactions per second.
     *
     * @return load in tx per second.
     */
    long getLoad();
}
