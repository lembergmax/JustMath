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

package com.mlprograms.justmath.calculator.errors;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lombok.NonNull;

/**
 * Immutable value object that describes a single failure produced by the {@code CalculatorEngine}.
 *
 * <p>
 * A {@code CalculatorError} carries:
 * </p>
 * <ul>
 *   <li>a {@link CalculatorErrorCode} that identifies the structural cause,</li>
 *   <li>a map of named parameters (such as {@code character}, {@code function} or
 *       {@code variable}) that are substituted into the localized template,</li>
 *   <li>a technical detail string in English suitable for logs and for the
 *       {@link ErrorMode#RAW} output, and</li>
 *   <li>an optional position inside the original expression.</li>
 * </ul>
 *
 * <p>
 * The {@link #format(Locale, ErrorMode)} method returns either the technical detail string or
 * a localized text taken from the resource bundle {@code i18n/calculator_errors}, depending
 * on the requested {@link ErrorMode}.
 * </p>
 *
 * @param code            the structured cause of this error; never {@code null}
 * @param params          named substitution parameters for the localized template; never {@code null}
 * @param technicalDetail technical English detail used in logs and {@link ErrorMode#RAW}; never {@code null}
 * @param position        optional one-based position of the failure inside the expression, or {@code null}
 */
public record CalculatorError(
        @NonNull CalculatorErrorCode code,
        @NonNull Map<String, String> params,
        @NonNull String technicalDetail,
        Integer position
) {

    /**
     * Base name of the resource bundle that stores the localized message templates.
     *
     * <p>
     * {@link ResourceBundle#getBundle(String, Locale)} resolves this base name to
     * {@code calculator_errors_de.properties}, {@code calculator_errors_en.properties} or
     * the default bundle, using the standard JDK locale fallback rules.
     * </p>
     */
    public static final String BUNDLE_BASENAME = "i18n.calculator_errors";

    /**
     * Convenience constructor that creates an error without parameters and without a position.
     *
     * @param code            the structured error code; must not be {@code null}
     * @param technicalDetail the technical English detail; must not be {@code null}
     */
    public CalculatorError(@NonNull final CalculatorErrorCode code, @NonNull final String technicalDetail) {
        this(code, Map.of(), technicalDetail, null);
    }

    /**
     * Convenience constructor that creates an error with parameters but without a position.
     *
     * @param code            the structured error code; must not be {@code null}
     * @param params          named substitution parameters for the localized template; must not be {@code null}
     * @param technicalDetail the technical English detail; must not be {@code null}
     */
    public CalculatorError(
            @NonNull final CalculatorErrorCode code,
            @NonNull final Map<String, String> params,
            @NonNull final String technicalDetail
    ) {
        this(code, params, technicalDetail, null);
    }

    /**
     * Formats this error according to the requested mode.
     *
     * <p>
     * In {@link ErrorMode#RAW} the technical detail is returned verbatim (English). In
     * {@link ErrorMode#USER_FRIENDLY} the matching resource bundle is loaded for the given
     * locale and the localized template is filled in via simple named-placeholder
     * substitution. If the bundle does not contain an entry for {@link #code()}, the method
     * defensively falls back to the technical detail so that no
     * {@link MissingResourceException} is propagated to the caller.
     * </p>
     *
     * @param locale target locale for the localized message; must not be {@code null}
     * @param mode   selected formatting mode; must not be {@code null}
     * @return the formatted message text; never {@code null}
     */
    public String format(@NonNull final Locale locale, @NonNull final ErrorMode mode) {
        if (mode == ErrorMode.RAW) {
            return technicalDetail;
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASENAME, locale);
            String template = bundle.getString(code.getBundleKey());
            return applyNamedParameters(template, params, position);
        } catch (MissingResourceException ignored) {
            return technicalDetail;
        }
    }

    /**
     * Substitutes named placeholders such as {@code {character}}, {@code {position}} or
     * {@code {function}} in the given template with the values from {@code params} and the
     * separately supplied position.
     *
     * <p>
     * Parameters are applied first; the position is only injected when the caller did not
     * already pass an explicit {@code position} entry in {@code params}.
     * </p>
     *
     * @param template the localized template containing placeholders in curly braces
     * @param params   named substitution parameters
     * @param position optional one-based position, or {@code null} to skip position substitution
     * @return the substituted text
     */
    private static String applyNamedParameters(
            @NonNull final String template,
            @NonNull final Map<String, String> params,
            final Integer position
    ) {
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        if (position != null) {
            result = result.replace("{position}", Integer.toString(position));
        }
        return result;
    }

}
