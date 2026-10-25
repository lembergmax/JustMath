package io.github.lembergmax.exception;

abstract class JustMathException extends RuntimeException {

    public JustMathException(
            final String message
    ) {
        super(message);
    }

}
