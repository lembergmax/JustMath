/*
 * Copyright (c) 2025-2026 Max Lemberg
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

import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Central configuration for locale handling used by the BigNumber parsing subsystem.
 *
 * <p>
 * This class defines which {@link Locale Locales} are considered during auto-detection of numeric formats
 * (e.g., deciding whether {@code "12,5"} uses comma as a decimal separator).
 * </p>
 *
 * <h2>Why this exists</h2>
 * <p>
 * {@link Locale#getAvailableLocales()} returns all locales known to the JVM in an unspecified order.
 * When implementing locale auto-detection, iterating that list directly can lead to surprising results,
 * because many locales share the same decimal/grouping symbols. As a consequence, the "first matching locale"
 * might be an unexpected one (for example {@code tk_TM_#Latn}).
 * </p>
 *
 * <h2>Strategy</h2>
 * <p>
 * {@link #SUPPORTED_LOCALES} is built as follows:
 * </p>
 * <ol>
 *   <li>Start with a curated, deterministic list of {@link #PREFERRED_LOCALES} to ensure stable behavior.</li>
 *   <li>Append all other JVM locales as a fallback, while keeping insertion order and removing duplicates.</li>
 * </ol>
 *
 * <p>
 * The resulting array is stable, unique, and prioritizes the most common locales first.
 * </p>
 */
@NoArgsConstructor
public final class LocalesConfig {

	/**
	 * Deterministic, curated list of locales that should be tried first during auto-detection.
	 *
	 * <p>
	 * This list is intentionally ordered to prioritize the most common numeric formats.
	 * Locales are included both as full country variants (e.g. {@link Locale#GERMANY})
	 * and, where useful, as language-only variants (e.g. {@link Locale#GERMAN}) to increase
	 * matching chances for systems that primarily set a language rather than a region.
	 * </p>
	 *
	 * <p>
	 * Note that this list does not need to be exhaustive; all other locales are appended afterwards
	 * via {@link Locale#getAvailableLocales()} in {@link #buildSupportedLocales()}.
	 * </p>
	 */
	private static final Locale[] PREFERRED_LOCALES = {
			Locale.US,                      // en_US
			Locale.UK,                      // en_GB
			Locale.GERMANY,                 // de_DE
			Locale.GERMAN,                  // de (language-only)
			Locale.forLanguageTag("de-AT"), // German (Austria)
			Locale.forLanguageTag("de-CH"), // German (Switzerland)
			Locale.CANADA,
			Locale.FRANCE,
			Locale.ITALY,
			Locale.forLanguageTag("es-ES"), // Spanish (Spain)
			Locale.forLanguageTag("pt-BR")  // Portuguese (Brazil)
	};

	/**
	 * Full list of locales used for numeric parsing auto-detection.
	 *
	 * <p>
	 * The array is built once during class initialization and contains:
	 * </p>
	 * <ul>
	 *   <li>all locales from {@link #PREFERRED_LOCALES} (in the exact order defined there)</li>
	 *   <li>followed by all locales returned by {@link Locale#getAvailableLocales()}</li>
	 * </ul>
	 *
	 * <p>
	 * Duplicates are removed while preserving insertion order.
	 * </p>
	 *
	 * <p>
	 * This constant is intended to be consumed by parsing logic (e.g. a locale resolver) that attempts
	 * to validate an input format against multiple locales.
	 * </p>
	 */
	public static final Locale[] SUPPORTED_LOCALES = buildSupportedLocales();

	/**
	 * Builds the prioritized {@link #SUPPORTED_LOCALES} array.
	 *
	 * <p>
	 * Implementation details:
	 * </p>
	 * <ul>
	 *   <li>{@link LinkedHashSet} is used to preserve insertion order while removing duplicates.</li>
	 *   <li>{@code null} locales are ignored defensively.</li>
	 * </ul>
	 *
	 * @return an array of unique locales where preferred locales come first, followed by all JVM locales
	 */
	private static Locale[] buildSupportedLocales() {
		final Set<Locale> ordered = new LinkedHashSet<>();

		// preferred first
		for (Locale locale : PREFERRED_LOCALES) {
			if (locale != null) {
				ordered.add(locale);
			}
		}

		// then all available
		for (Locale locale : Locale.getAvailableLocales()) {
			if (locale != null) {
				ordered.add(locale);
			}
		}

		return ordered.toArray(Locale[]::new);
	}

}