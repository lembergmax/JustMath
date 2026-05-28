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

package com.mlprograms.justmath;

import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.errors.ErrorMode;

import java.util.Locale;

/**
 * Minimal runnable demonstration of the {@link CalculatorEngine} API.
 *
 * <p>This class is a developer convenience entry point only. It is intentionally
 * excluded from the published library jar (see the {@code maven-jar-plugin} and
 * {@code maven-source-plugin} configuration in {@code pom.xml}) and carries no library
 * logic — keep production behaviour and tests out of here.</p>
 */
public final class Main {

    private Main() {
        // Demonstration entry point; not instantiable.
    }

    public static void main(final String[] args) {
        final CalculatorEngine engine = new CalculatorEngine()
                .setLocale(Locale.GERMAN)
                .setErrorMode(ErrorMode.USER_FRIENDLY);

        // A successful evaluation rendered with the configured locale.
        System.out.println("1/2 + 1/2 = " + engine.evaluateToString("1/2 + 1/2"));

        // A failing evaluation, folded into a localized error string (Text Output API).
        System.out.println("5/0      = " + engine.evaluateToString("5/0"));
    }

}
