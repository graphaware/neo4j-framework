package com.graphaware.runtime.bootstrap;

import org.neo4j.graphdb.GraphDatabaseService;

import com.graphaware.runtime.config.ScheduleConfiguration;
import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;

/**
 * A {@link ComponentFactory} that is not configurable and always uses the default implementation of each component type.
 */
public class DefaultImplementationComponentFactory implements ComponentFactory {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation always returns an {@link AdaptiveTimingStrategy} based on the given {@link ScheduleConfiguration}.
	 * </p>
	 */
	@Override
	public TimingStrategy createTimingStrategy(GraphDatabaseService database, ScheduleConfiguration scheduleConfig) {
		return new AdaptiveTimingStrategy(database, scheduleConfig);
	}

}
