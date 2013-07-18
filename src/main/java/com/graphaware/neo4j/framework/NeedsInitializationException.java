package com.graphaware.neo4j.framework;

/**
 * {@link RuntimeException} indicating a {@link GraphAwareModule} needs to be (re-)initialized.
 */
public class NeedsInitializationException extends RuntimeException {

    public NeedsInitializationException() {
    }

    public NeedsInitializationException(String message) {
        super(message);
    }

    public NeedsInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeedsInitializationException(Throwable cause) {
        super(cause);
    }

    public NeedsInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
