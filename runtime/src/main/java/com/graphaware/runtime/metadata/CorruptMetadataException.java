package com.graphaware.runtime.metadata;

/**
 *
 */
public class CorruptMetadataException extends RuntimeException {

    public CorruptMetadataException() {
    }

    public CorruptMetadataException(String message) {
        super(message);
    }

    public CorruptMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

    public CorruptMetadataException(Throwable cause) {
        super(cause);
    }

    public CorruptMetadataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
