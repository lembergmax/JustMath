package com.mlprograms.justmath.graphing.api;

/**
 * Raised when an expression cannot be evaluated during sampling.
 */
public class GraphingEvaluationException extends GraphingException {

    public GraphingEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
