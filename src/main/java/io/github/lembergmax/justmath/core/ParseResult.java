package io.github.lembergmax.justmath.core;

/**
 * The result of parsing a number from text.
 *
 * @param beforeDecimals the digits before the decimal separator, without sign and grouping separators
 * @param afterDecimals  the digits after the decimal separator, "0" if there are none
 * @param isNegative     whether the number has a minus sign, never true for a zero
 */
public record ParseResult(
        String beforeDecimals,
        String afterDecimals,
        boolean isNegative
) {
}
