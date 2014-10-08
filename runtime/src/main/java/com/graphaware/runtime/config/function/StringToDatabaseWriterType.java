package com.graphaware.runtime.config.function;

import com.graphaware.runtime.write.DatabaseWriterType;
import org.neo4j.helpers.Function;

/**
 * A {@link org.neo4j.helpers.Function} that converts String to {@link com.graphaware.runtime.schedule.TimingStrategy}. Singleton.
 * <p/>
 * Converts "fixed" to {@link com.graphaware.runtime.schedule.FixedDelayTimingStrategy} and "adaptive" to {@link com.graphaware.runtime.schedule.AdaptiveTimingStrategy}.
 */
public final class StringToDatabaseWriterType implements Function<String, DatabaseWriterType> {

    public static final String DEFAULT = "default";
    public static final String SINGLE_THREAD = "single";
    public static final String BATCH = "batch";

    private static StringToDatabaseWriterType INSTANCE = new StringToDatabaseWriterType();

    public static StringToDatabaseWriterType getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseWriterType apply(String s) {
        if (s.equalsIgnoreCase(DEFAULT)) {
            return DatabaseWriterType.DEFAULT;
        }

        if (s.equalsIgnoreCase(SINGLE_THREAD)) {
            return DatabaseWriterType.SINGLE_THREADED;
        }

        if (s.equalsIgnoreCase(BATCH)) {
            return DatabaseWriterType.BATCH;
        }

        throw new IllegalStateException("Unknown database writer: " + s);
    }
}
