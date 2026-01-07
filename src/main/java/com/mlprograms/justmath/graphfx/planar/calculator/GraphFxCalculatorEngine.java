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

package com.mlprograms.justmath.graphfx.planar.calculator;

import com.mlprograms.justmath.bignumber.BigNumber;
import com.mlprograms.justmath.bignumber.BigNumbers;
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.graphfx.ReservedVariables;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import com.mlprograms.justmath.graphfx.planar.view.ViewportSnapshot;
import lombok.NonNull;

import java.util.Map;
import java.util.Set;

public class GraphFxCalculatorEngine {

    private final CalculatorEngine CALCULATOR_ENGINE = new CalculatorEngine(TrigonometricMode.RAD);

    private BigNumber x;
    private BigNumber y;

    public PlotResult evaluate(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final ViewportSnapshot viewportSnapshot) {
        if (!isRequestValid(expression, variables, viewportSnapshot)) {
            return new PlotResult();
        }

        // TODO: implement the actual plotting algorithm (sampling + discontinuity detection + line splitting).
        // For now we only validate and provide the view-derived boundaries.

        return new PlotResult();
    }

    private boolean isRequestValid(final String expression, final Map<String, String> variables, final ViewportSnapshot viewportSnapshot) {
        if (expression.isBlank()) {
            return false;
        }

        if (variablesContainReservedVariables(variables.keySet())) {
            return false;
        }

        if (viewportSnapshot == null) {
            return false;
        }

        if (viewportSnapshot.cellSize().isLessThanOrEqualTo(BigNumbers.ZERO)) {
            return false;
        }

        return areMinMaxValid(viewportSnapshot.minX(), viewportSnapshot.maxX(), viewportSnapshot.minY(), viewportSnapshot.maxY());
    }

    private boolean variablesContainReservedVariables(final Set<String> variableNames) {
        for (final String variableName : variableNames) {
            if (variableName == null) {
                continue;
            }

            if (variableName.equals(ReservedVariables.X.getValue()) || variableName.equals(ReservedVariables.Y.getValue())) {
                return true;
            }
        }

        return false;
    }

    private boolean areMinMaxValid(final BigNumber minX, final BigNumber maxX, final BigNumber minY, final BigNumber maxY) {
        if (minX.isGreaterThanOrEqualTo(maxX)) {
            return false;
        }
        if (minY.isGreaterThanOrEqualTo(maxY)) {
            return false;
        }

        return true;
    }

}
