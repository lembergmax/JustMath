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

package com.mlprograms.justmath.bignumber.matrix;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lombok.NonNull;

/**
 * Localized message catalog for matrix errors.
 *
 * <p>
 * Resolves message templates against {@code i18n/matrix_errors_{lang}.properties} via the
 * standard {@link ResourceBundle} fallback chain (e.g. {@code _de} for German JVMs, falling
 * back to {@code _en} and finally the root bundle for any other locale).
 * </p>
 *
 * <p>
 * Mirrors the design of {@link com.mlprograms.justmath.calculator.errors.CalculatorError}:
 * placeholders are written as {@code {name}} and are substituted with simple
 * {@link String#replace(CharSequence, CharSequence)} calls — no {@link java.text.MessageFormat}
 * is involved, so apostrophes in the templates do <em>not</em> need MessageFormat escaping.
 * </p>
 *
 * <p>
 * This helper is intentionally stateless and side-effect free, and resolves the bundle on
 * each call. If a bundle key cannot be resolved, the key itself is returned so that callers
 * still produce a deterministic, debuggable exception message.
 * </p>
 */
public final class MatrixMessages {

    /**
     * Base name of the resource bundle that stores matrix message templates.
     */
    public static final String BUNDLE_BASENAME = "i18n.matrix_errors";

    private MatrixMessages() {
        // utility class
    }

    /**
     * Returns the localized template for {@code key} without parameter substitution.
     */
    public static String get(@NonNull final Locale locale, @NonNull final String key) {
        return get(locale, key, Map.of());
    }

    /**
     * Returns the localized template for {@code key} with the given {@code params} substituted
     * into matching {@code {placeholder}} occurrences.
     *
     * <p>
     * If no template is registered for {@code key} in any bundle on the lookup chain, the
     * key itself is returned. This is intentional: we prefer a stable, recognizable fallback
     * over throwing {@link MissingResourceException} from an error-reporting helper.
     * </p>
     *
     * @param locale target locale; selects between {@code _de}, {@code _en}, root
     * @param key    bundle key, e.g. {@code "matrix.error.multiplyDimMismatch"}
     * @param params named substitution values
     * @return the localized, parameter-substituted message
     */
    public static String get(
            @NonNull final Locale locale,
            @NonNull final String key,
            @NonNull final Map<String, String> params
    ) {
        String template;
        try {
            template = ResourceBundle.getBundle(BUNDLE_BASENAME, locale).getString(key);
        } catch (MissingResourceException ignored) {
            return key;
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return template;
    }

}
