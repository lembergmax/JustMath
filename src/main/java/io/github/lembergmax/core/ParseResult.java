package io.github.lembergmax.core;

import java.util.Locale;

public record ParseResult(
        String beforeDecimals,
        String afterDecimals,
        boolean isNegative,
        Locale locale
) {
}
