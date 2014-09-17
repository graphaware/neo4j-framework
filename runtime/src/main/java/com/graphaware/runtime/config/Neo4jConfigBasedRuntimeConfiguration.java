package com.graphaware.runtime.config;

import static org.neo4j.helpers.Settings.INTEGER;
import static org.neo4j.helpers.Settings.LONG;
import static org.neo4j.helpers.Settings.setting;

import com.graphaware.runtime.config.function.StringToTimingStrategy;
import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.kernel.configuration.Config;

/**
 * Implementation of {@link RuntimeConfiguration} that loads bespoke settings from Neo4j's configuration properties, falling
 * back to default values when overrides aren't available. Intended for internal framework use, mainly for server deployments.
 * <p/>
 * So far, the only thing that is configured using this mechanism is the {@link TimingStrategy} for the {@link com.graphaware.runtime.GraphAwareRuntime}.
 * <p/>
 * There are two choices: {@link AdaptiveTimingStrategy}, configured by using the following settings
 * <pre>
 *     com.graphaware.runtime.timing.strategy=adaptive
 *     com.graphaware.runtime.timing.delay=2000
 *     com.graphaware.runtime.timing.maxDelay=5000
 *     com.graphaware.runtime.timing.minDelay=5
 *     com.graphaware.runtime.timing.busyThreshold=100
 *     com.graphaware.runtime.timing.maxSamples=200
 *     com.graphaware.runtime.timing.maxTime=2000
 *
 * </pre>
 * The above are also the default values, if no configuration is provided. For exact meaning of the values, please refer
 * to the Javadoc of {@link AdaptiveTimingStrategy}.
 * <p/>
 * The other option is {@link FixedDelayTimingStrategy}, configured by using the following settings
 * <pre>
 *     com.graphaware.runtime.timing.strategy=fixed
 *     com.graphaware.runtime.timing.delay=200
 *     com.graphaware.runtime.timing.initialDelay=1000
 * </pre>
 */
public class Neo4jConfigBasedRuntimeConfiguration extends BaseRuntimeConfiguration {

    private static final Setting<TimingStrategy> TIMING_STRATEGY_SETTING = setting("com.graphaware.runtime.timing.strategy", StringToTimingStrategy.getInstance(), (String) null);

    //for both strategies, this is the main (default, mean, whatever) delay
    private static final Setting<Long> DELAY_SETTING = setting("com.graphaware.runtime.timing.delay", LONG, (String) null);

    //for FixedDelayTimingStrategy only
    private static final Setting<Long> INITIAL_DELAY_SETTING = setting("com.graphaware.runtime.timing.initialDelay", LONG, (String) null);

    //for AdaptiveTimingStrategy only
    private static final Setting<Long> MAX_DELAY_SETTING = setting("com.graphaware.runtime.timing.maxDelay", LONG, (String) null);
    private static final Setting<Long> MIN_DELAY_SETTING = setting("com.graphaware.runtime.timing.minDelay", LONG, (String) null);
    private static final Setting<Integer> BUSY_THRESHOLD_SETTING = setting("com.graphaware.runtime.timing.busyThreshold", INTEGER, (String) null);
    private static final Setting<Integer> MAX_SAMPLES_SETTING = setting("com.graphaware.runtime.timing.maxSamples", INTEGER, (String) null);
    private static final Setting<Integer> MAX_TIME_SETTING = setting("com.graphaware.runtime.timing.maxTime", INTEGER, (String) null);

    private final Config config;

    /**
     * Constructs a new {@link Neo4jConfigBasedRuntimeConfiguration} based on the given Neo4j {@link Config}.
     *
     * @param config The {@link Config} containing the settings used to configure the runtime
     */
    public Neo4jConfigBasedRuntimeConfiguration(Config config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimingStrategy getTimingStrategy() {
        TimingStrategy timingStrategy = config.get(TIMING_STRATEGY_SETTING);

        if (timingStrategy == null) {
            return AdaptiveTimingStrategy.defaultConfiguration();
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
}
