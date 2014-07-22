package com.graphaware.runtime.config;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.kernel.configuration.Config;

public class Neo4jConfigBasedRuntimeConfigurationTest {

	@Test
	public void shouldUseValueSpecifiedInConfig() {
		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("com.graphaware.runtime.schedule.activityThreshold", "94");
		Config config = new Config(parameterMap);

		assertEquals(94, new Neo4jConfigBasedRuntimeConfiguration(config).getScheduleConfiguration().databaseActivityThreshold());
	}

	@Test
	public void shouldFallBackToValueInDefaultConfigurationIfValueIsNotFoundInConfig() {
		Map<String, String> parameterMap = new HashMap<>();
		parameterMap.put("com.graphaware.runtime.schedule.defaultDelay", "1500");
		parameterMap.put("com.graphaware.runtime.schedule.minDelay", "50");
		Config config = new Config(parameterMap);

		long defaultMaximumDelay = DefaultRuntimeConfiguration.getInstance().maximumDelayMillis();
		assertEquals(defaultMaximumDelay, new Neo4jConfigBasedRuntimeConfiguration(config).getScheduleConfiguration().maximumDelayMillis());
	}

}
