/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.config;

import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import org.junit.Test;
import org.neo4j.kernel.configuration.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Neo4jConfigBasedRuntimeConfigurationTest {

    @Test
    public void shouldUseValuesSpecifiedInConfig() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.timing.strategy", "adaptive");
        parameterMap.put("com.graphaware.runtime.timing.delay", "50");
        parameterMap.put("com.graphaware.runtime.timing.maxDelay", "100");
        parameterMap.put("com.graphaware.runtime.timing.minDelay", "10");
        parameterMap.put("com.graphaware.runtime.timing.busyThreshold", "94");
        parameterMap.put("com.graphaware.runtime.timing.maxSamples", "201");
        parameterMap.put("com.graphaware.runtime.timing.maxTime", "2001");
        Config config = Config.empty().with(parameterMap);

        TimingStrategy expected = AdaptiveTimingStrategy
                .defaultConfiguration()
                .withBusyThreshold(94)
                .withDefaultDelayMillis(50)
                .withMinimumDelayMillis(10)
                .withMaximumDelayMillis(100)
                .withMaxSamples(201)
                .withMaxTime(2001);

        assertEquals(expected, new Neo4jConfigBasedRuntimeConfiguration(null, config).getTimingStrategy());
    }

    @Test
    public void shouldUseValuesSpecifiedInConfig2() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.timing.strategy", "fixed");
        parameterMap.put("com.graphaware.runtime.timing.initialDelay", "100");
        parameterMap.put("com.graphaware.runtime.timing.delay", "50");
        Config config = Config.empty().with(parameterMap);

        TimingStrategy expected = FixedDelayTimingStrategy
                .getInstance()
                .withDelay(50)
                .withInitialDelay(100);

        assertEquals(expected, new Neo4jConfigBasedRuntimeConfiguration(null, config).getTimingStrategy());
    }

    @Test
    public void shouldFallBackToValueDefaultConfigurationIfValueIsNotFoundInConfig() {
        Map<String, String> parameterMap = new HashMap<>();
        Config config = Config.empty().with(parameterMap);

        TimingStrategy expected = AdaptiveTimingStrategy
                .defaultConfiguration();

        assertEquals(expected, new Neo4jConfigBasedRuntimeConfiguration(null, config).getTimingStrategy());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWithUnknownStrategy() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.timing.strategy", "unknown");
        Config config = Config.empty().with(parameterMap);

        new Neo4jConfigBasedRuntimeConfiguration(null, config).getTimingStrategy();
    }
}
