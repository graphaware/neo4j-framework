package com.graphaware.runtime.metadata;

/**
 * An {@link Exception} indicating that a position in the graph could not be found.
 */
public class PositionNotFoundException extends Exception {

    public PositionNotFoundException() {
    }

    public PositionNotFoundException(String message) {
        super(message);
    }

    public PositionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PositionNotFoundException(Throwable cause) {
        super(cause);
    }

    public PositionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
