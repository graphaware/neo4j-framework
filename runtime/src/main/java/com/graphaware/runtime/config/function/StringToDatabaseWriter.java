package com.graphaware.runtime.config.function;

import com.graphaware.writer.BatchWriter;
import com.graphaware.writer.DatabaseWriter;
import com.graphaware.writer.DefaultWriter;
import com.graphaware.writer.TxPerTaskWriter;
import org.neo4j.helpers.Function;

/**
 * A {@link org.neo4j.helpers.Function} that converts String to {@link com.graphaware.runtime.schedule.TimingStrategy}. Singleton.
 * <p/>
 * Converts "fixed" to {@link com.graphaware.runtime.schedule.FixedDelayTimingStrategy} and "adaptive" to {@link com.graphaware.runtime.schedule.AdaptiveTimingStrategy}.
 */
public final class StringToDatabaseWriter implements Function<String, DatabaseWriter> {

    public static final String DEFAULT = "default";
    public static final String SINGLE_THREAD = "single";
    public static final String BATCH = "batch";

    private static StringToDatabaseWriter INSTANCE = new StringToDatabaseWriter();

    public static StringToDatabaseWriter getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseWriter apply(String s) {
        if (s.equalsIgnoreCase(DEFAULT)) {
            return DefaultWriter.getInstance();
        }

        if (s.equalsIgnoreCase(SINGLE_THREAD)) {
            //todo configure queue size
            return new TxPerTaskWriter();
        }

        if (s.equalsIgnoreCase(BATCH)) {
            //todo configure queue size
            return new BatchWriter();
        }

        throw new IllegalStateException("Unknown database writer: " + s);
    }
}
