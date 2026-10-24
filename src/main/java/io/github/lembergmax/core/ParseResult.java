package io.github.lembergmax.core;

import java.util.Locale;

public record ParseResult(
        String beforeDecimals,
        String afterDecimals,
        Locale locale
) {
}
