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

package com.mlprograms.justmath;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.graphing.eval.ExpressionEngine;
import com.mlprograms.justmath.graphing.eval.GraphFxCalculatorEngine;

import java.util.HashMap;
import java.util.Map;

public final class Main {

    public static void main(final String[] args) {
        final ExpressionEngine engine = GraphFxCalculatorEngine.createDefault();

        final BigNumber simple = engine.evaluate("2*(3+4)");
        System.out.println("2*(3+4) = " + simple);

        final Map<String, String> variables = new HashMap<>();
        variables.put("a", "5+3");
        variables.put("b", "3");

        final BigNumber withVars = engine.evaluate("2*a + b^2", variables);
        System.out.println("2*a + b^2 (a=5+3, b=3) = " + withVars);

        // Example for the future graph engine hot path: "evaluate at x".
        final BigNumber atX = engine.compile("sin(x)").evaluateAtX(12345678.9d, Map.of());
        System.out.println("sin(x) at x=12345678.9 = " + atX);
    }

}