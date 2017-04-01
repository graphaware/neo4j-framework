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
import com.graphaware.common.ping.NullStatsCollector;
import com.graphaware.common.ping.StatsCollector;
import com.graphaware.runtime.config.function.StringToDatabaseWriterType;
import com.graphaware.runtime.config.function.StringToTimingStrategy;
import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import com.graphaware.runtime.write.DatabaseWriterType;
import com.graphaware.runtime.write.FluentWritingConfig;
import com.graphaware.runtime.write.WritingConfig;
import com.graphaware.writer.neo4j.BatchWriter;
import com.graphaware.writer.neo4j.DefaultWriter;
import com.graphaware.writer.neo4j.TxPerTaskWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.kernel.configuration.Config;

import static org.neo4j.kernel.configuration.Settings.*;

/**
 * Implementation of {@link RuntimeConfiguration} that loads bespoke settings from Neo4j's configuration properties, falling
 * back to default values when overrides aren't available. Intended for internal framework use, mainly for server deployments.
 * <p>
 * There are three main things configured using this mechanism: the {@link TimingStrategy}, the {@link DatabaseWriterType}, and the {@link StatsCollector}.
 * <p>
 * For {@link TimingStrategy}, there are two choices. The first one is {@link AdaptiveTimingStrategy}, configured by using the following settings
 * <pre>
 *     com.graphaware.runtime.timing.strategy=adaptive
 *     com.graphaware.runtime.timing.delay=2000
 *     com.graphaware.runtime.timing.maxDelay=5000
 *     com.graphaware.runtime.timing.minDelay=5
 *     com.graphaware.runtime.timing.busyThreshold=100
 *     com.graphaware.runtime.timing.maxSamples=200
 *     com.graphaware.runtime.timing.maxTime=2000*
 * </pre>
 * The above are also the default values, if no configuration is provided. For exact meaning of the values, please refer
 * to the Javadoc of {@link AdaptiveTimingStrategy}.
 * <p>
 * The other option is {@link FixedDelayTimingStrategy}, configured by using the following settings
 * <pre>
 *     com.graphaware.runtime.timing.strategy=fixed
 *     com.graphaware.runtime.timing.delay=200
 *     com.graphaware.runtime.timing.initialDelay=1000
 * </pre>
 * <p>
 * For {@link WritingConfig}, there are three choices:
 * <pre>
 *     com.graphaware.runtime.db.writer=default
 * </pre>
 * results in a {@link DefaultWriter} being constructed.
 * <p>
 * <pre>
 *     com.graphaware.runtime.db.writer=single
 *     #optional queue size, defaults to 10,000
 *     com.graphaware.runtime.db.writer.queueSize=10000
 * </pre>
 * results in a {@link TxPerTaskWriter} being constructed with the configured queue size
 * <p>
 * <pre>
 *     com.graphaware.runtime.db.writer=batch
 *     #optional queue size, defaults to 10,000
 *     com.graphaware.runtime.db.writer.queueSize=10000
 *     #optional batch size, defaults to 1,000
 *     com.graphaware.runtime.db.writer.batchSize=1000
 * </pre>
 * results in a {@link BatchWriter} being constructed with the configured queue and batch sizes.
 * <p>
 * For {@link StatsCollector}, {@link GoogleAnalyticsStatsCollector} is used by default. For disabling statistics reporting, use
 * <pre>
 *     com.graphaware.runtime.stats.disable=true
 * </pre>
 * With this setting in place, {@link NullStatsCollector} will be used.
 */
public final class Neo4jConfigBasedRuntimeConfiguration extends BaseRuntimeConfiguration {

    //writer
    private static final Setting<DatabaseWriterType> DATABASE_WRITER_TYPE_SETTING = setting("com.graphaware.runtime.db.writer", StringToDatabaseWriterType.getInstance(), (String) null);
    private static final Setting<Integer> WRITER_QUEUE_SIZE = setting("com.graphaware.runtime.db.writer.queueSize", INTEGER, (String) null);
    private static final Setting<Integer> WRITER_BATCH_SIZE = setting("com.graphaware.runtime.db.writer.batchSize", INTEGER, (String) null);

    //timing
    private static final Setting<TimingStrategy> TIMING_STRATEGY_SETTING = setting("com.graphaware.runtime.timing.strategy", StringToTimingStrategy.getInstance(), (String) null);

    //for both policies, this is the main (default, mean, whatever) delay
    private static final Setting<Long> DELAY_SETTING = setting("com.graphaware.runtime.timing.delay", LONG, (String) null);

    //for FixedDelayTimingStrategy only
    private static final Setting<Long> INITIAL_DELAY_SETTING = setting("com.graphaware.runtime.timing.initialDelay", LONG, (String) null);

    //for AdaptiveTimingStrategy only
    private static final Setting<Long> MAX_DELAY_SETTING = setting("com.graphaware.runtime.timing.maxDelay", LONG, (String) null);
    private static final Setting<Long> MIN_DELAY_SETTING = setting("com.graphaware.runtime.timing.minDelay", LONG, (String) null);
    private static final Setting<Integer> BUSY_THRESHOLD_SETTING = setting("com.graphaware.runtime.timing.busyThreshold", INTEGER, (String) null);
    private static final Setting<Integer> MAX_SAMPLES_SETTING = setting("com.graphaware.runtime.timing.maxSamples", INTEGER, (String) null);
    private static final Setting<Integer> MAX_TIME_SETTING = setting("com.graphaware.runtime.timing.maxTime", INTEGER, (String) null);

    //stats
    //see https://github.com/graphaware/neo4j-framework/issues/59
    private static final Setting<Boolean> STATS_DISABLE_SETTING_LEGACY = setting("com.graphaware.runtime.stats.disable", BOOLEAN, "false");
    private static final Setting<Boolean> STATS_DISABLE_SETTING = setting("com.graphaware.runtime.stats.disabled", BOOLEAN, "false");

    /**
     * Constructs a new {@link Neo4jConfigBasedRuntimeConfiguration} based on the given Neo4j {@link Config}.
     *
     * @param config The {@link Config} containing the settings used to configure the runtime
     */
    public Neo4jConfigBasedRuntimeConfiguration(GraphDatabaseService database, Config config) {
        super(createTimingStrategy(config), createWritingConfig(config), createStatsCollector(database, config));
    }

    private static TimingStrategy createTimingStrategy(Config config) {
        TimingStrategy timingStrategy = config.get(TIMING_STRATEGY_SETTING);

        if (timingStrategy == null) {
            timingStrategy = AdaptiveTimingStrategy.defaultConfiguration();
        }

        if (timingStrategy instanceof FixedDelayTimingStrategy) {
            FixedDelayTimingStrategy strategy = (FixedDelayTimingStrategy) timingStrategy;

            if (config.get(INITIAL_DELAY_SETTING) != null) {
                strategy = strategy.withInitialDelay(config.get(INITIAL_DELAY_SETTING));
            }

            if (config.get(DELAY_SETTING) != null) {
                strategy = strategy.withDelay(config.get(DELAY_SETTING));
            }

            return strategy;
        }

        if (timingStrategy instanceof AdaptiveTimingStrategy) {
            AdaptiveTimingStrategy strategy = (AdaptiveTimingStrategy) timingStrategy;

            if (config.get(DELAY_SETTING) != null) {
                strategy = strategy.withDefaultDelayMillis(config.get(DELAY_SETTING));
            }

            if (config.get(MAX_DELAY_SETTING) != null) {
                strategy = strategy.withMaximumDelayMillis(config.get(MAX_DELAY_SETTING));
            }

            if (config.get(MIN_DELAY_SETTING) != null) {
                strategy = strategy.withMinimumDelayMillis(config.get(MIN_DELAY_SETTING));
            }

            if (config.get(BUSY_THRESHOLD_SETTING) != null) {
                strategy = strategy.withBusyThreshold(config.get(BUSY_THRESHOLD_SETTING));
            }

            if (config.get(MAX_SAMPLES_SETTING) != null) {
                strategy = strategy.withMaxSamples(config.get(MAX_SAMPLES_SETTING));
            }

            if (config.get(MAX_TIME_SETTING) != null) {
                strategy = strategy.withMaxTime(config.get(MAX_TIME_SETTING));
            }

            return strategy;
        }

        throw new IllegalStateException("Unknown timing strategy!");
    }

    private static WritingConfig createWritingConfig(Config config) {
        DatabaseWriterType databaseWriterType = config.get(DATABASE_WRITER_TYPE_SETTING);

        FluentWritingConfig result = FluentWritingConfig.defaultConfiguration();

        if (databaseWriterType != null) {
            result = result.withWriterType(databaseWriterType);
        }

        if (config.get(WRITER_QUEUE_SIZE) != null) {
            result = result.withQueueSize(config.get(WRITER_QUEUE_SIZE));
        }

        if (config.get(WRITER_BATCH_SIZE) != null) {
            result = result.withBatchSize(config.get(WRITER_BATCH_SIZE));
        }

        return result;
    }

    private static StatsCollector createStatsCollector(GraphDatabaseService database, Config config) {
        if (config.get(STATS_DISABLE_SETTING_LEGACY)) {
            return NullStatsCollector.getInstance();
        }

        if (config.get(STATS_DISABLE_SETTING)) {
            return NullStatsCollector.getInstance();
        }

        return new GoogleAnalyticsStatsCollector(database);
    }
}
