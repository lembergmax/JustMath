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

package io.github.lembergmax.justmath.calculator;

import java.util.List;
import java.util.Locale;

import lombok.NonNull;

/**
 * Single source of truth for the languages JustMath ships translated messages for.
 *
 * <p>
 * "Language support" in JustMath means three things, all of which apply to every locale listed
 * here:
 * </p>
 * <ul>
 *   <li><strong>Localized error messages</strong> — both the calculator error catalog
 *       ({@code i18n/calculator_errors_*.properties}) and the matrix error catalog
 *       ({@code i18n/matrix_errors_*.properties}) provide a fully translated bundle for the
 *       locale, resolved through the standard {@link java.util.ResourceBundle} fallback chain.</li>
 *   <li><strong>Locale-aware number formatting</strong> — decimal and grouping separators for
 *       result output are taken from the JDK for the active locale (e.g. {@code "1.234,56"} under
 *       German/French/Spanish, {@code "1,234.56"} under English).</li>
 *   <li><strong>Locale-aware input parsing</strong> — see
 *       {@link CalculatorEngine#setLocale(Locale)}: when the active locale uses {@code ','} as its
 *       decimal separator, expressions such as {@code "3,14"} are accepted in addition to the
 *       always-valid {@code '.'} form.</li>
 * </ul>
 *
 * <p>
 * Consuming applications (for example a calculator front end) should call {@link #all()} to learn
 * exactly which languages they may offer, instead of hard-coding a list of their own. A locale that
 * is <em>not</em> listed here still works — it simply falls back to English messages and JDK number
 * formatting; it is just not advertised as a first-class supported language.
 * </p>
 *
 * <p>
 * The list intentionally distinguishes regional variants where they carry a meaningful difference
 * for JustMath (for example {@code de-CH}, which writes {@code ss} instead of {@code ß}, or
 * {@code pt-PT} vs {@code pt-BR}). Each listed variant has its own resource bundle.
 * </p>
 *
 * <p>This class is stateless and thread-safe.</p>
 */
public final class SupportedLanguages {

    /**
     * Immutable, ordered list of locales JustMath provides translated message bundles for.
     *
     * <p>
     * The order is purely cosmetic (English and German first, then the remaining languages
     * roughly alphabetically); callers must not rely on it. Each entry corresponds to a
     * {@code calculator_errors_<tag>.properties} <em>and</em> {@code matrix_errors_<tag>.properties}
     * file on the classpath (or, for plain language tags, to the bundle reached via the
     * {@link java.util.ResourceBundle} fallback chain).
     * </p>
     */
    private static final List<Locale> SUPPORTED = List.of(
            Locale.ENGLISH,                   // en
            Locale.GERMAN,                    // de
            Locale.forLanguageTag("de-AT"),   // German (Austria)
            Locale.forLanguageTag("de-CH"),   // German (Switzerland) — ß written as ss
            Locale.forLanguageTag("es"),      // Spanish
            Locale.forLanguageTag("fr"),      // French
            Locale.forLanguageTag("fr-BE"),   // French (Belgium)
            Locale.forLanguageTag("it"),      // Italian
            Locale.forLanguageTag("pt"),      // Portuguese (European baseline)
            Locale.forLanguageTag("pt-PT"),   // Portuguese (Portugal)
            Locale.forLanguageTag("pt-BR"),   // Portuguese (Brazil)
            Locale.forLanguageTag("nl"),      // Dutch
            Locale.forLanguageTag("nl-BE"),   // Dutch (Belgium / Flemish)
            Locale.forLanguageTag("cs"),      // Czech
            Locale.forLanguageTag("da"),      // Danish
            Locale.forLanguageTag("sv"),      // Swedish
            Locale.forLanguageTag("no"),      // Norwegian (macrolanguage)
            Locale.forLanguageTag("nb"),      // Norwegian Bokmål
            Locale.forLanguageTag("fi"),      // Finnish
            Locale.forLanguageTag("pl")       // Polish
    );

    /** Non-instantiable utility class. */
    private SupportedLanguages() {
    }

    /**
     * Returns the immutable list of locales JustMath ships translated messages for.
     *
     * @return an unmodifiable, non-empty list of supported locales; never {@code null}
     */
    public static List<Locale> all() {
        return SUPPORTED;
    }

    /**
     * Reports whether JustMath advertises first-class support for the given locale.
     *
     * <p>
     * A locale counts as supported if it is contained in {@link #all()} either exactly (matching
     * language <em>and</em> country, e.g. {@code pt-BR}) or at language level (e.g. {@code fr-CA}
     * is supported because French is supported). Country-only or script-only matches do not count.
     * </p>
     *
     * @param locale the locale to test; must not be {@code null}
     * @return {@code true} if the locale or its language is supported, {@code false} otherwise
     */
    public static boolean isSupported(@NonNull final Locale locale) {
        if (SUPPORTED.contains(locale)) {
            return true;
        }
        final String language = locale.getLanguage();
        if (language.isEmpty()) {
            return false;
        }
        for (final Locale supported : SUPPORTED) {
            if (supported.getLanguage().equals(language)) {
                return true;
            }
        }
        return false;
    }

}
