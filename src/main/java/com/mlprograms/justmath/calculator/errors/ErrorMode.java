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

/**
 * Steuert den Detaillierungsgrad und die Sprache der von der {@code CalculatorEngine}
 * nach außen gegebenen Fehlertexte.
 *
 * <p>
 * <strong>RAW</strong> liefert technische Meldungen (englisch, mit internen Details wie Token,
 * Positionen oder Stack-Größen). Geeignet für Logs, Debugging und Bibliotheks-Konsumenten,
 * die strukturiert mit {@link CalculatorErrorCode} arbeiten und nur den Originalkontext brauchen.
 * </p>
 *
 * <p>
 * <strong>USER_FRIENDLY</strong> liefert lokalisierte, nutzerfreundliche Texte über die
 * Ressourcen-Bundles unter {@code i18n/calculator_errors_*.properties}. Die Auswahl der
 * Locale erfolgt über {@link com.mlprograms.justmath.calculator.CalculatorEngine#setLocale(java.util.Locale)}
 * oder pro Auswertung.
 * </p>
 */
public enum ErrorMode {

    /**
     * Technische, englische Fehlermeldung mit internen Details (Standard).
     */
    RAW,

    /**
     * Lokalisierte, nutzerfreundliche Fehlermeldung über das Resource-Bundle.
     */
    USER_FRIENDLY
}
