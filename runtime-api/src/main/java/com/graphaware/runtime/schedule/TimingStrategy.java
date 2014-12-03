package com.graphaware.runtime.schedule;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A strategy for timing scheduled tasks.
 */
public interface TimingStrategy {

    public static final int UNKNOWN = -1;
    public static final int NEVER_RUN = -2;

    /**
     * Initialize the timing strategy before it can be used.
     *
     * @param database against which the runtime using this strategy runs.
     */
    void initialize(GraphDatabaseService database);

    /**
     * Get the delay until the timer fires again.
     *
     * @param lastTaskDuration The total time it took to execute the last task in milliseconds. -1 if unknown.
     * @return The next delay in <b>milliseconds</b>.
     */
    long nextDelay(long lastTaskDuration);

}
