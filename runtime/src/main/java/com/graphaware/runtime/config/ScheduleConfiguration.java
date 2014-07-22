package com.graphaware.runtime.config;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.schedule.TimingStrategy;

/**
 * Interface to access the settings used to configure timer-driven components of the runtime, such as the task scheduler and
 * timing strategies.
 */
public interface ScheduleConfiguration {

	long defaultDelayMillis();

	long minimumDelayMillis();

	long maximumDelayMillis();

	int databaseActivityThreshold();

}
