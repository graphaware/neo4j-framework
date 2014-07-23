package com.graphaware.runtime.bootstrap;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.config.ScheduleConfiguration;
import com.graphaware.runtime.schedule.TimingStrategy;

/**
 * Responsible for creating components for use in the current <code>GraphAwareRuntime</code> based on the selected configuration
 * settings and preferred implementations specified for a runtime.
 */
public interface ComponentFactory {

	/**
	 * Creates a timing strategy to use for configuring the way in which timer-driven modules are managed for the given
	 * database.
	 *
	 * @param graphDatabaseService The {@link GraphDatabaseService} in which the <code>GraphAwareRuntime</code> will be used.
	 * @return The {@link TimingStrategy} to use in the <code>GraphAwareRuntime</code>.
	 */
	TimingStrategy createTimingStrategy(GraphDatabaseService database, ScheduleConfiguration config);

}
