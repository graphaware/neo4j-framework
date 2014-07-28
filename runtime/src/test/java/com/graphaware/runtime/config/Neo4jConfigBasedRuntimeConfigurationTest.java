package com.graphaware.runtime.config;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import org.junit.Test;
import org.neo4j.kernel.configuration.Config;

public class Neo4jConfigBasedRuntimeConfigurationTest {

    @Test
    public void shouldUseValuesSpecifiedInConfig() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.timing.strategy", "adaptive");
        parameterMap.put("com.graphaware.runtime.timing.delay", "50");
        parameterMap.put("com.graphaware.runtime.timing.maxDelay", "100");
        parameterMap.put("com.graphaware.runtime.timing.minDelay", "10");
        parameterMap.put("com.graphaware.runtime.timing.activityThreshold", "94");
        parameterMap.put("com.graphaware.runtime.timing.maxSamples", "201");
        parameterMap.put("com.graphaware.runtime.timing.maxTime", "2001");
        Config config = new Config(parameterMap);

        TimingStrategy expected = AdaptiveTimingStrategy
                .defaultConfiguration()
                .withBusyThreshold(94)
                .withDefaultDelayMillis(50)
                .withMinimumDelayMillis(10)
                .withMaximumDelayMillis(100)
                .withMaxSamples(201)
                .withMaxTime(2001);

        assertEquals(expected, new Neo4jConfigBasedRuntimeConfiguration(config).getTimingStrategy());
    }

    @Test
    public void shouldUseValuesSpecifiedInConfig2() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.timing.strategy", "fixed");
        parameterMap.put("com.graphaware.runtime.timing.initialDelay", "100");
        parameterMap.put("com.graphaware.runtime.timing.delay", "50");
        Config config = new Config(parameterMap);

        TimingStrategy expected = FixedDelayTimingStrategy
                .getInstance()
                .withDelay(50)
                .withInitialDelay(100);

        assertEquals(expected, new Neo4jConfigBasedRuntimeConfiguration(config).getTimingStrategy());
    }

    @Test
    public void shouldFallBackToValueDefaultConfigurationIfValueIsNotFoundInConfig() {
        Map<String, String> parameterMap = new HashMap<>();
        Config config = new Config(parameterMap);

        TimingStrategy expected = AdaptiveTimingStrategy
                .defaultConfiguration();

        assertEquals(expected, new Neo4jConfigBasedRuntimeConfiguration(config).getTimingStrategy());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWithUnknownStrategy() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.timing.strategy", "unknown");
        Config config = new Config(parameterMap);

        new Neo4jConfigBasedRuntimeConfiguration(config).getTimingStrategy();
    }
}
