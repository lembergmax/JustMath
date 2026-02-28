package com.mlprograms.justmath.graphing.api;

/**
 * Base unchecked exception for graphing failures.
 */
public class GraphingException extends RuntimeException {

    public GraphingException(final String message) {
        super(message);
    }

    public GraphingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
