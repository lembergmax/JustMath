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
import com.mlprograms.justmath.graphfx.planar.model.PlotRequest;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphFxCalculatorEngine {

    private final CalculatorEngine CALCULATOR_ENGINE = new CalculatorEngine(TrigonometricMode.RAD);

    private BigNumber x;
    private BigNumber y;

    private PlotResult evaluate(@NonNull final PlotRequest plotRequest) {
        if (!isPlotRequestDataValid(plotRequest)) {
            return new PlotResult();
        }

        final Map<String, String> combinedVariables = new HashMap<>(plotRequest.getVariables());
        combinedVariables.put(ReservedVariables.X.getValue(), x.toString());
        combinedVariables.put(ReservedVariables.Y.getValue(), y.toString());

        final BigNumber result = CALCULATOR_ENGINE.evaluate(plotRequest.getExpression(), combinedVariables);
        return new PlotResult();
    }

    private boolean isPlotRequestDataValid(final PlotRequest plotRequest) {
        if (plotRequest == null) {
            return false;
        }

        if (plotRequest.getExpression().isBlank()) {
            return false;
        }

        if (plotRequest.getCellSize().isLessThanOrEqualTo(BigNumbers.ZERO)) {
            return false;
        }

        if (!isMinMaxValid(plotRequest.getMinX(), plotRequest.getMaxX(), plotRequest.getMinY(), plotRequest.getMaxY())) {
            return false;
        }

        if (variablesContainReservedVariable(plotRequest.getVariables().keySet())) {
            return false;
        }

        return true;
    }

    private boolean variablesContainReservedVariable(final Set<String> variablesKeySet) {
        for (final String key : variablesKeySet) {
            if (key.equals(ReservedVariables.X.getValue())) {
                return true;
            }
            if (key.equals(ReservedVariables.Y.getValue())) {
                return true;
            }
        }

        return false;
    }

    private boolean isMinMaxValid(final BigNumber minX, final BigNumber maxX, final BigNumber minY, final BigNumber maxY) {
        if (minX.isGreaterThanOrEqualTo(maxX)) {
            return false;
        }

        if (minY.isGreaterThanOrEqualTo(maxY)) {
            return false;
        }

        return true;
    }

}
