package com.graphaware.generator;

/**
 * Exception signifying an invalid distribution used for graph generation.
 */
public class InvalidDistributionException extends RuntimeException {

    public InvalidDistributionException() {
    }

    public InvalidDistributionException(String message) {
        super(message);
    }

    public InvalidDistributionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDistributionException(Throwable cause) {
        super(cause);
    }

    public InvalidDistributionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
