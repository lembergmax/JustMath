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
 * Controls the verbosity and language of error messages emitted by the
 * {@code CalculatorEngine}.
 *
 * <p>
 * <strong>{@link #RAW}</strong> emits the technical, English detail message including internal
 * context such as tokens, positions or stack sizes. It is intended for log output, debugging
 * and for library consumers who already use {@link CalculatorErrorCode} for structured
 * handling and only need the original technical context.
 * </p>
 *
 * <p>
 * <strong>{@link #USER_FRIENDLY}</strong> emits a localized, end-user oriented message taken
 * from the resource bundles located at {@code i18n/calculator_errors_*.properties}. The
 * effective locale is configured via
 * {@link com.mlprograms.justmath.calculator.CalculatorEngine#setLocale(java.util.Locale)}.
 * </p>
 */
public enum ErrorMode {

    /**
     * Technical, English error message including internal context. This is the default and
     * preserves the behaviour of older releases that pre-date the localization layer.
     */
    RAW,

    /**
     * Localized, user-facing error message taken from the resource bundle.
     */
    USER_FRIENDLY
}
