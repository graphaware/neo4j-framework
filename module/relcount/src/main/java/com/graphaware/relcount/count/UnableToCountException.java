package com.graphaware.relcount.count;

/**
 * {@link RuntimeException} indicating that for some reason, relationships could not be counted. For example, when asking
 * for a count purely based on cached values and the cached values are not present (e.g. have been compacted-out).
 */
public class UnableToCountException extends RuntimeException {

    public UnableToCountException() {
    }

    public UnableToCountException(String message) {
        super(message);
    }

    public UnableToCountException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnableToCountException(Throwable cause) {
        super(cause);
    }

    public UnableToCountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
