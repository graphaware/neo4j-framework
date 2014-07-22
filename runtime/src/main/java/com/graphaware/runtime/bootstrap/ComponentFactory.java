package com.graphaware.runtime.bootstrap;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.config.ScheduleConfiguration;
import com.graphaware.runtime.schedule.TimingStrategy;

/**
 * Responsible for creating components for use in the current <code>GraphAwareRuntime</code> based on the selected configuration
 * settings and preferred implementations.
 */
public interface ComponentFactory {

	/**
	 * Creates a timing strategy to use for configuring the way in which timer-driven modules are managed for the given
	 * database.
	 * <p>
	 * <strong>Design Note</strong><br/>
	 * There's an argument that suggests we don't really want instances of {@link ScheduleConfiguration} to be creating complex
	 * classes because it's too much of a mix of responsibilities.  It's arguably better to just have them provide the settings
	 * that are needed and let a factory or constructor decide how to use them.  Then again, it's more testable this way, and if
	 * it's possible to configure the timing strategy implementation class then this is not unreasonable.
	 * </p>
	 *
	 * @param graphDatabaseService The {@link GraphDatabaseService} in which the <code>GraphAwareRuntime</code> will be used
	 * @return The {@link TimingStrategy} to use in the <code>GraphAwareRuntime</code>
	 */
	TimingStrategy createTimingStrategy(GraphDatabaseService database, ScheduleConfiguration config);

}
