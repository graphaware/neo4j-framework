package com.graphaware.runtime.config;

import static org.neo4j.helpers.Settings.INTEGER;
import static org.neo4j.helpers.Settings.LONG;
import static org.neo4j.helpers.Settings.setting;

import org.neo4j.graphdb.config.Setting;
import org.neo4j.kernel.configuration.Config;

import com.graphaware.runtime.bootstrap.ComponentFactory;

/**
 * Implementation of {@link RuntimeConfiguration} that loads bespoke settings from Neo4j's configuration properties, falling
 * back to the corresponding {@link DefaultRuntimeConfiguration} when overrides aren't available.
 */
public class Neo4jConfigBasedRuntimeConfiguration extends BaseRuntimeConfiguration implements ScheduleConfiguration {

	private static final Setting<Integer> ACTIVITY_THRESHOLD_SETTING = setting("com.graphaware.runtime.schedule.activityThreshold", INTEGER, (String) null);
	private static final Setting<Long> DEFAULT_DELAY_SETTING = setting("com.graphaware.runtime.schedule.defaultDelay", LONG, (String) null);
	private static final Setting<Long> MAX_DELAY_SETTING = setting("com.graphaware.runtime.schedule.maxDelay", LONG, (String) null);
	private static final Setting<Long> MIN_DELAY_SETTING = setting("com.graphaware.runtime.schedule.minDelay", LONG, (String) null);

	private final Config config;

	/**
	 * Constructs a new {@link Neo4jConfigBasedRuntimeConfiguration} based on the given Neo4j {@link Config}.
	 *
	 * @param config The {@link Config} containing the settings used to configure the runtime
	 */
	public Neo4jConfigBasedRuntimeConfiguration(Config config) {
		this.config = config;
	}

	@Override
	public int databaseActivityThreshold() {
		Integer result = config.get(ACTIVITY_THRESHOLD_SETTING);
		return result != null ? result : getDefault().databaseActivityThreshold();
	}

	@Override
	public long defaultDelayMillis() {
		Long result = config.get(DEFAULT_DELAY_SETTING);
		return result != null ? result : getDefault().defaultDelayMillis();
	}

	@Override
	public long maximumDelayMillis() {
		Long result = config.get(MAX_DELAY_SETTING);
		return result != null ? result : getDefault().maximumDelayMillis();
	}

	@Override
	public long minimumDelayMillis() {
		Long result = config.get(MIN_DELAY_SETTING);
		return result != null ? result : getDefault().minimumDelayMillis();
	}

	@Override
	public ScheduleConfiguration getScheduleConfiguration() {
		return this;
	}

	@Override
	public ComponentFactory getComponentFactory() {
		return getDefault().getComponentFactory();
	}

	private DefaultRuntimeConfiguration getDefault() {
		return DefaultRuntimeConfiguration.getInstance();
	}

}
