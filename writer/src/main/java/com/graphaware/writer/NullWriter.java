package com.graphaware.writer;

import java.util.concurrent.Callable;

/**
 * A {@link DatabaseWriter} that throws an {@link UnsupportedOperationException} any time it is used for writing. Its
 * purpose is to serve as a placeholder for places where the use of {@link DatabaseWriter} does not make sense, e.g.
 * when using {@link org.neo4j.unsafe.batchinsert.BatchInserter}s. Singleton.
 */
public final class NullWriter implements DatabaseWriter {

    private static final NullWriter INSTANCE = new NullWriter();

    /**
     * Get an instance of this writer.
     *
     * @return instance.
     */
    public static NullWriter getInstance() {
        return INSTANCE;
    }

    private NullWriter() {
    }

    @Override
    public void start() {
        //no-op
    }

    @Override
    public void stop() {
        //no-op
    }

    @Override
    public void write(Runnable task) {
        throwException();
    }

    @Override
    public void write(Runnable task, String id) {
        throwException();
    }

    @Override
    public <T> T write(Callable<T> task, String id, int waitMillis) {
        throwException();
        return null;
    }

    private void throwException() {
        throw new UnsupportedOperationException("NullWriter should not be used for writing to the database. Are you using it in batch inserter mode?");
    }
}
