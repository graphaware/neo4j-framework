package com.graphaware.runtime.config;

import com.graphaware.runtime.schedule.AdaptiveTimingStrategy;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import org.neo4j.helpers.Function;

/**
 * A {@link Function} that converts String to {@link TimingStrategy}. Singleton.
 * <p/>
 * Converts "fixed" to {@link FixedDelayTimingStrategy} and "adaptive" to {@link AdaptiveTimingStrategy}.
 */
public final class StringToTimingStrategy implements Function<String, TimingStrategy> {

    public static final String FIXED = "fixed";
    public static final String ADAPTIVE = "adaptive";

    private static StringToTimingStrategy INSTANCE = new StringToTimingStrategy();

    public static StringToTimingStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimingStrategy apply(String s) {
        if (s.equalsIgnoreCase(FIXED)) {
            return FixedDelayTimingStrategy.getInstance();
        }

        if (s.equalsIgnoreCase(ADAPTIVE)) {
            return AdaptiveTimingStrategy.defaultConfiguration();
        }

        throw new IllegalStateException("Unknown timing strategy: " + s);
    }
}
