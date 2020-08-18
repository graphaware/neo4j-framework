/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime.config;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.kernel.configuration.Config;

import com.graphaware.common.ping.GoogleAnalyticsStatsCollector;
import com.graphaware.common.ping.NullStatsCollector;

public class Neo4jConfigBasedRuntimeConfigurationTest {
    
    @Test
    public void shouldDisableGoogleAnalytics() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.stats.disabled", "true");

        Config config = Config.defaults(parameterMap);

        assertEquals(NullStatsCollector.getInstance(), new Neo4jConfigBasedRuntimeConfiguration(null, config).getStatsCollector());
    }
    
    @Test
    public void shouldEnableGoogleAnalytics() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("com.graphaware.runtime.stats.disabled", "false");

        Config config = Config.defaults(parameterMap);

        assertEquals(GoogleAnalyticsStatsCollector.class.getName(), new Neo4jConfigBasedRuntimeConfiguration(null, config).getStatsCollector().getClass().getName());
    }

}
