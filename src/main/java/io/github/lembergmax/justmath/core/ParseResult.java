package io.github.lembergmax.justmath.core;

import java.util.Locale;

public record ParseResult(
        String beforeDecimals,
        String afterDecimals,
        boolean isNegative,
        Locale locale
) {
}
