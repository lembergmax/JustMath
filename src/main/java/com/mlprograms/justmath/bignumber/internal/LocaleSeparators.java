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

import lombok.NonNull;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable container für locale-spezifische Trennzeichen mit globalem Cache.
 *
 * <p>
 * Vermeidet wiederholtes {@link DecimalFormatSymbols#getInstance(Locale)} in Hot-Paths
 * (Tokenizer, Parser, Number-Formatter). Pro Locale wird genau eine Instanz erzeugt
 * und in einer {@link ConcurrentHashMap} gehalten.
 * </p>
 *
 * @param decimalSeparator  Dezimaltrennzeichen der Locale
 * @param groupingSeparator Gruppierungstrennzeichen der Locale
 */
public record LocaleSeparators(char decimalSeparator, char groupingSeparator) {

    private static final ConcurrentHashMap<Locale, LocaleSeparators> CACHE = new ConcurrentHashMap<>();

    /**
     * Liefert die Trennzeichen für die übergebene Locale aus dem Cache; berechnet sie
     * beim ersten Zugriff via {@link DecimalFormatSymbols#getInstance(Locale)}.
     *
     * @param locale Locale; darf nicht {@code null} sein
     * @return gecachte Trennzeichen für diese Locale; niemals {@code null}
     */
    public static LocaleSeparators forLocale(@NonNull final Locale locale) {
        return CACHE.computeIfAbsent(locale, l -> {
            final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(l);
            return new LocaleSeparators(symbols.getDecimalSeparator(), symbols.getGroupingSeparator());
        });
    }

}
