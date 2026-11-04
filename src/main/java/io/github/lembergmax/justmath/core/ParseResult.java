package io.github.lembergmax.justmath.core;

// TODO: javadoc
public record ParseResult(
        String beforeDecimals,
        String afterDecimals,
        boolean isNegative
) {
}
