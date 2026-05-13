/*
 * Copyright (c) 2026 Max Lemberg
 *
 * This file is part of JustMath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mlprograms.justmath.bignumber.internal;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;

/**
 * Immutable container for locale-specific decimal and grouping separators with a process-wide cache.
 *
 * <p>
 * This type avoids repeated invocations of {@link DecimalFormatSymbols#getInstance(Locale)} in
 * hot paths such as the tokenizer, the BigNumber parser and number formatting routines. Each
 * locale is resolved at most once and the resulting separators are kept in a
 * {@link ConcurrentHashMap} that is shared across all callers.
 * </p>
 *
 * <p>
 * Instances are immutable and safe to share between threads.
 * </p>
 *
 * @param decimalSeparator  the locale-specific decimal separator character
 * @param groupingSeparator the locale-specific grouping (thousands) separator character
 */
public record LocaleSeparators(char decimalSeparator, char groupingSeparator) {

    /**
     * Process-wide cache mapping each previously resolved locale to its separators.
     */
    private static final ConcurrentHashMap<Locale, LocaleSeparators> CACHE = new ConcurrentHashMap<>();

    /**
     * Returns the cached separators for the given locale, computing them on first access
     * via {@link DecimalFormatSymbols#getInstance(Locale)}.
     *
     * <p>
     * Subsequent calls for the same locale are served from the cache without any further
     * lookup of {@link DecimalFormatSymbols}.
     * </p>
     *
     * @param locale the locale to resolve separators for; must not be {@code null}
     * @return cached decimal and grouping separators for the given locale; never {@code null}
     */
    public static LocaleSeparators forLocale(@NonNull final Locale locale) {
        return CACHE.computeIfAbsent(locale, l -> {
            final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(l);
            return new LocaleSeparators(symbols.getDecimalSeparator(), symbols.getGroupingSeparator());
        });
    }

}
