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

import com.graphaware.common.ping.GoogleAnalyticsStatsCollector;
import com.graphaware.common.ping.StatsCollector;
import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import com.graphaware.runtime.write.FluentWritingConfig;
import com.graphaware.runtime.write.WritingConfig;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * {@link RuntimeConfiguration} for {@link com.graphaware.runtime.GraphAwareRuntime} with fluent interface.
 * Intended for users of Neo4j in embedded mode to programatically configure the runtime.
 */
public final class FluentRuntimeConfiguration extends BaseRuntimeConfiguration {

    /**
     * Creates an instance with default values.
     *
     * @return The {@link FluentRuntimeConfiguration} instance.
     */
    public static FluentRuntimeConfiguration defaultConfiguration(GraphDatabaseService database) {
        return new FluentRuntimeConfiguration(AdaptiveTimingStrategy.defaultConfiguration(), FluentWritingConfig.defaultConfiguration(), new GoogleAnalyticsStatsCollector(database));
    }

    private FluentRuntimeConfiguration(TimingStrategy timingStrategy, WritingConfig writingConfig, StatsCollector statsCollector) {
        super(timingStrategy, writingConfig, statsCollector);
    }

    /**
     * Create an instance with different {@link TimingStrategy}.
     *
     * @param timingStrategy of the new instance.
     * @return new instance.
     */
    public FluentRuntimeConfiguration withTimingStrategy(TimingStrategy timingStrategy) {
        return new FluentRuntimeConfiguration(timingStrategy, getWritingConfig(), getStatsCollector());
    }

    /**
     * Create an instance with different {@link WritingConfig}.
     *
     * @param writingConfig of the new instance.
     * @return new instance.
     */
    public FluentRuntimeConfiguration withWritingConfig(WritingConfig writingConfig) {
        return new FluentRuntimeConfiguration(getTimingStrategy(), writingConfig, getStatsCollector());
    }

    /**
     * Create an instance with different {@link StatsCollector}.
     *
     * @param statsCollector of the new instance.
     * @return new instance.
     */
    public FluentRuntimeConfiguration withStatsCollector(StatsCollector statsCollector) {
        return new FluentRuntimeConfiguration(getTimingStrategy(), getWritingConfig(), statsCollector);
    }
}
