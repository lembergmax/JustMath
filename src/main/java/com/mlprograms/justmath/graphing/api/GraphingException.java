package com.mlprograms.justmath.graphing.api;

/**
 * Base exception for graphing API failures.
 */
public class GraphingException extends RuntimeException {

    public GraphingException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphingException(String message) {
        super(message);
    }
}
