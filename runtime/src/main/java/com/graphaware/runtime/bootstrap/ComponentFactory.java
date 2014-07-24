package com.graphaware.runtime.bootstrap;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.config.ScheduleConfiguration;
import com.graphaware.runtime.schedule.TimingStrategy;

/**
 * Responsible for creating components for use in the current {@link com.graphaware.runtime.GraphAwareRuntime} based on
 * the selected configuration settings and preferred implementations specified for a runtime.
 */
public interface ComponentFactory {

	/**
	 * Creates a timing strategy to use for configuring the way in which timer-driven modules are managed for the given
	 * database.
	 *
	 * @param database The {@link GraphDatabaseService} in which the {@link com.graphaware.runtime.GraphAwareRuntime} will be used.
	 * @return The {@link TimingStrategy} to use in the {@link com.graphaware.runtime.GraphAwareRuntime}.
	 */
	TimingStrategy createTimingStrategy(GraphDatabaseService database, ScheduleConfiguration config);

}
