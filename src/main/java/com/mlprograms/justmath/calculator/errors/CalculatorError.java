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

import lombok.NonNull;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Unveränderlicher Wertcontainer für einen konkreten Fehler der {@code CalculatorEngine}.
 *
 * <p>
 * Trägt:
 * </p>
 * <ul>
 *   <li>einen {@link CalculatorErrorCode} (strukturierte Ursache),</li>
 *   <li>benannte Parameter (z. B. {@code character}, {@code position}, {@code function}),
 *       die in der lokalisierten Meldung eingesetzt werden,</li>
 *   <li>eine technische Detailmeldung (englisch, eignet sich für Logs und {@link ErrorMode#RAW}),</li>
 *   <li>optional eine Position im Ausdruck.</li>
 * </ul>
 *
 * <p>
 * Die {@link #format(Locale, ErrorMode)}-Methode liefert je nach {@link ErrorMode} entweder
 * den technischen Detailtext oder einen lokalisierten Text aus dem Ressourcen-Bundle
 * {@code i18n/calculator_errors}.
 * </p>
 */
public record CalculatorError(
        @NonNull CalculatorErrorCode code,
        @NonNull Map<String, String> params,
        @NonNull String technicalDetail,
        Integer position
) {

    /**
     * Basisname des Ressourcen-Bundles. Über {@link Locale}-Fallback findet
     * {@link ResourceBundle#getBundle(String, Locale)} automatisch
     * {@code calculator_errors_de.properties}, {@code _en.properties} oder das Default.
     */
    public static final String BUNDLE_BASENAME = "i18n.calculator_errors";

    /**
     * Erstellt einen Fehler ohne Parameter und ohne Position.
     *
     * @param code             der strukturierte Fehlercode
     * @param technicalDetail technische, englische Detailmeldung
     */
    public CalculatorError(@NonNull final CalculatorErrorCode code, @NonNull final String technicalDetail) {
        this(code, Map.of(), technicalDetail, null);
    }

    /**
     * Erstellt einen Fehler mit Parametern aber ohne Position.
     *
     * @param code             der strukturierte Fehlercode
     * @param params           benannte Platzhalter für das Bundle
     * @param technicalDetail technische, englische Detailmeldung
     */
    public CalculatorError(
            @NonNull final CalculatorErrorCode code,
            @NonNull final Map<String, String> params,
            @NonNull final String technicalDetail
    ) {
        this(code, params, technicalDetail, null);
    }

    /**
     * Formatiert den Fehler je nach Modus.
     *
     * <p>
     * Bei {@link ErrorMode#RAW} wird der technische Detailtext zurückgegeben (englisch).
     * Bei {@link ErrorMode#USER_FRIENDLY} wird das passende Ressourcen-Bundle für die
     * angegebene Locale geladen und das Template über einfache benannte
     * Platzhalter-Ersetzung mit den Parametern befüllt. Fehlt ein Schlüssel im Bundle,
     * fällt die Methode defensiv auf den technischen Text zurück.
     * </p>
     *
     * @param locale Ziel-Locale für die lokalisierte Meldung
     * @param mode   gewählter Modus
     * @return formatierter Meldungstext (nie {@code null})
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
     * Ersetzt benannte Platzhalter wie {@code {character}}, {@code {position}}, {@code {function}}
     * im Template durch die Werte aus {@code params} bzw. der separat übergebenen Position.
     *
     *
     * @param template Bundle-Template mit Platzhaltern in geschweiften Klammern
     * @param params   benannte Parameter
     * @param position optionale Position
     * @return finaler Text
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
