/*
 * Copyright (c) 2025 Max Lemberg
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

package com.mlprograms.justmath.graph;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * GraphFunction implementation backed by CalculatorEngine for expressions like "sin(x)+x^2".
 */
@RequiredArgsConstructor
public class CalculatorEngineGraphFunction implements GraphFunction {

    @NonNull
    private final CalculatorEngine calculatorEngine;
    @NonNull
    private final String expression;
    @NonNull
    private final Map<String, String> baseVariables;

    public CalculatorEngineGraphFunction(@NonNull final CalculatorEngine calculatorEngine, @NonNull final String expression) {
        this(calculatorEngine, expression, Map.of());
    }

    @Override
    public BigNumber evaluate(@NonNull final BigDecimal x) {
        final Map<String, String> variables = new HashMap<>(baseVariables);
        variables.put("x", x.stripTrailingZeros().toPlainString());
        return calculatorEngine.evaluate(expression, variables);
    }

}
