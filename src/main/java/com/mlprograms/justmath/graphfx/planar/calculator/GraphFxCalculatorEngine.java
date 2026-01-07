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
import com.mlprograms.justmath.calculator.CalculatorEngine;
import com.mlprograms.justmath.calculator.internal.TrigonometricMode;
import com.mlprograms.justmath.graphfx.ReservedVariables;
import com.mlprograms.justmath.graphfx.planar.model.PlotLine;
import com.mlprograms.justmath.graphfx.planar.model.PlotPoint;
import com.mlprograms.justmath.graphfx.planar.model.PlotResult;
import com.mlprograms.justmath.graphfx.planar.view.ViewportSnapshot;
import lombok.NonNull;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static com.mlprograms.justmath.bignumber.BigNumbers.TWO;
import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

public final class GraphFxCalculatorEngine {

    private static final int MAXIMUM_GRID_POINT_COUNT = 2_000_000;

    private final CalculatorEngine calculatorEngine = new CalculatorEngine(new MathContext(10, RoundingMode.HALF_UP), TrigonometricMode.RAD);

    public PlotResult evaluate(@NonNull final String expression, @NonNull final ViewportSnapshot viewportSnapshot) {
        return evaluate(expression, Map.of(), viewportSnapshot);
    }

    public PlotResult evaluate(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final ViewportSnapshot viewportSnapshot) {
        if (!isRequestValid(expression, variables, viewportSnapshot)) {
            return new PlotResult();
        }

        final BigNumber minimumXValue = viewportSnapshot.minX();
        final BigNumber maximumXValue = viewportSnapshot.maxX();
        final BigNumber minimumYValue = viewportSnapshot.minY();
        final BigNumber maximumYValue = viewportSnapshot.maxY();
        final BigNumber cellSize = viewportSnapshot.cellSize();

        final BigNumber[] xAxisValues = createGridAxisValues(minimumXValue, maximumXValue, cellSize);
        final BigNumber[] yAxisValues = createGridAxisValues(minimumYValue, maximumYValue, cellSize);

        if (xAxisValues.length < 2 || yAxisValues.length < 2) {
            return new PlotResult();
        }

        final long gridPointCount = (long) xAxisValues.length * (long) yAxisValues.length;
        if (gridPointCount > MAXIMUM_GRID_POINT_COUNT) {
            return new PlotResult();
        }

        final String[] xAxisValueStrings = convertBigNumbersToStrings(xAxisValues);
        final String[] yAxisValueStrings = convertBigNumbersToStrings(yAxisValues);

        final Map<String, String> combinedVariables = new HashMap<>(variables);

        BigNumber[] lowerGridRowValues = evaluateGridRow(expression, combinedVariables, xAxisValueStrings, yAxisValueStrings[0]);
        BigNumber[] upperGridRowValues = evaluateGridRow(expression, combinedVariables, xAxisValueStrings, yAxisValueStrings[1]);

        final int xAxisCellCount = xAxisValues.length - 1;
        final int yAxisCellCount = yAxisValues.length - 1;

        final int initialPlotLineCapacity = Math.min(16_384, xAxisCellCount * 4);
        final List<PlotLine> plotLines = new ArrayList<>(initialPlotLineCapacity);

        for (int yAxisCellIndex = 0; yAxisCellIndex < yAxisCellCount; yAxisCellIndex++) {
            final BigNumber lowerYValue = yAxisValues[yAxisCellIndex];
            final BigNumber upperYValue = yAxisValues[yAxisCellIndex + 1];

            for (int xAxisCellIndex = 0; xAxisCellIndex < xAxisCellCount; xAxisCellIndex++) {
                final BigNumber leftXValue = xAxisValues[xAxisCellIndex];
                final BigNumber rightXValue = xAxisValues[xAxisCellIndex + 1];

                final BigNumber bottomLeftCornerValue = lowerGridRowValues[xAxisCellIndex];
                final BigNumber bottomRightCornerValue = lowerGridRowValues[xAxisCellIndex + 1];
                final BigNumber topRightCornerValue = upperGridRowValues[xAxisCellIndex + 1];
                final BigNumber topLeftCornerValue = upperGridRowValues[xAxisCellIndex];

                if (bottomLeftCornerValue == null || bottomRightCornerValue == null || topRightCornerValue == null || topLeftCornerValue == null) {
                    continue;
                }

                if (canSkipCellBecauseNoContourCanExist(bottomLeftCornerValue, bottomRightCornerValue, topRightCornerValue, topLeftCornerValue)) {
                    continue;
                }

                appendPlotLinesForCell(plotLines, expression, combinedVariables, leftXValue, lowerYValue, rightXValue, upperYValue, bottomLeftCornerValue, bottomRightCornerValue, topRightCornerValue, topLeftCornerValue);
            }

            final int nextUpperRowIndex = yAxisCellIndex + 2;
            if (nextUpperRowIndex < yAxisValueStrings.length) {
                lowerGridRowValues = upperGridRowValues;
                upperGridRowValues = evaluateGridRow(expression, combinedVariables, xAxisValueStrings, yAxisValueStrings[nextUpperRowIndex]);
            }
        }

        return new PlotResult(new ArrayList<>(), plotLines);
    }

    private static BigNumber[] createGridAxisValues(final BigNumber minimumValue, final BigNumber maximumValue, final BigNumber stepSize) {
        final List<BigNumber> axisValues = new ArrayList<>();

        for (BigNumber currentValue = minimumValue; !currentValue.isGreaterThan(maximumValue); currentValue = currentValue.add(stepSize)) {
            axisValues.add(currentValue);
        }

        return axisValues.toArray(new BigNumber[0]);
    }

    private static String[] convertBigNumbersToStrings(final BigNumber[] values) {
        final String[] stringValues = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            stringValues[index] = values[index].toString();
        }
        return stringValues;
    }

    private BigNumber[] evaluateGridRow(final String expression, final Map<String, String> combinedVariables, final String[] xAxisValueStrings, final String yAxisValueString) {
        final BigNumber[] rowValues = new BigNumber[xAxisValueStrings.length];

        combinedVariables.put(ReservedVariables.Y.getValue(), yAxisValueString);

        for (int xAxisIndex = 0; xAxisIndex < xAxisValueStrings.length; xAxisIndex++) {
            combinedVariables.put(ReservedVariables.X.getValue(), xAxisValueStrings[xAxisIndex]);

            try {
                rowValues[xAxisIndex] = calculatorEngine.evaluate(expression, combinedVariables);
            } catch (final Exception ignored) {
                rowValues[xAxisIndex] = null;
            }
        }

        return rowValues;
    }

    private static boolean canSkipCellBecauseNoContourCanExist(final BigNumber bottomLeftCornerValue, final BigNumber bottomRightCornerValue, final BigNumber topRightCornerValue, final BigNumber topLeftCornerValue) {
        final int bottomLeftSign = bottomLeftCornerValue.signum();
        final int bottomRightSign = bottomRightCornerValue.signum();
        final int topRightSign = topRightCornerValue.signum();
        final int topLeftSign = topLeftCornerValue.signum();

        final boolean cellContainsExactZero = bottomLeftSign == 0 || bottomRightSign == 0 || topRightSign == 0 || topLeftSign == 0;

        if (cellContainsExactZero) {
            return false;
        }

        final boolean allCornerValuesArePositive = bottomLeftSign > 0 && bottomRightSign > 0 && topRightSign > 0 && topLeftSign > 0;

        final boolean allCornerValuesAreNegative = bottomLeftSign < 0 && bottomRightSign < 0 && topRightSign < 0 && topLeftSign < 0;

        return allCornerValuesArePositive || allCornerValuesAreNegative;
    }

    private void appendPlotLinesForCell(final List<PlotLine> plotLines, final String expression, final Map<String, String> combinedVariables, final BigNumber leftXValue, final BigNumber lowerYValue, final BigNumber rightXValue, final BigNumber upperYValue, final BigNumber bottomLeftCornerValue, final BigNumber bottomRightCornerValue, final BigNumber topRightCornerValue, final BigNumber topLeftCornerValue) {

        final PlotPoint bottomIntersectionPoint = computeIntersectionPointIfContourCrossesEdge(leftXValue, lowerYValue, bottomLeftCornerValue, rightXValue, lowerYValue, bottomRightCornerValue);

        final PlotPoint rightIntersectionPoint = computeIntersectionPointIfContourCrossesEdge(rightXValue, lowerYValue, bottomRightCornerValue, rightXValue, upperYValue, topRightCornerValue);

        final PlotPoint topIntersectionPoint = computeIntersectionPointIfContourCrossesEdge(rightXValue, upperYValue, topRightCornerValue, leftXValue, upperYValue, topLeftCornerValue);

        final PlotPoint leftIntersectionPoint = computeIntersectionPointIfContourCrossesEdge(leftXValue, upperYValue, topLeftCornerValue, leftXValue, lowerYValue, bottomLeftCornerValue);

        final PlotPoint[] uniqueIntersectionPoints = new PlotPoint[4];
        int uniqueIntersectionPointCount = 0;

        uniqueIntersectionPointCount = addUniquePlotPoint(uniqueIntersectionPoints, uniqueIntersectionPointCount, bottomIntersectionPoint);
        uniqueIntersectionPointCount = addUniquePlotPoint(uniqueIntersectionPoints, uniqueIntersectionPointCount, rightIntersectionPoint);
        uniqueIntersectionPointCount = addUniquePlotPoint(uniqueIntersectionPoints, uniqueIntersectionPointCount, topIntersectionPoint);
        uniqueIntersectionPointCount = addUniquePlotPoint(uniqueIntersectionPoints, uniqueIntersectionPointCount, leftIntersectionPoint);

        if (uniqueIntersectionPointCount == 0) {
            return;
        }

        if (uniqueIntersectionPointCount == 2) {
            plotLines.add(new PlotLine(List.of(uniqueIntersectionPoints[0], uniqueIntersectionPoints[1])));
            return;
        }

        if (uniqueIntersectionPointCount != 4) {
            return;
        }

        final int marchingSquaresMask = computeMarchingSquaresMask(bottomLeftCornerValue, bottomRightCornerValue, topRightCornerValue, topLeftCornerValue);

        if (marchingSquaresMask == 5 || marchingSquaresMask == 10) {
            final BigNumber centerXValue = leftXValue.add(rightXValue).divide(TWO);
            final BigNumber centerYValue = lowerYValue.add(upperYValue).divide(TWO);

            final BigNumber centerValue = tryEvaluateAt(expression, combinedVariables, centerXValue, centerYValue);
            final boolean centerValueIsPositive = centerValue != null && centerValue.isGreaterThan(ZERO);

            if (marchingSquaresMask == 5) {
                if (centerValueIsPositive) {
                    plotLines.add(new PlotLine(List.of(bottomIntersectionPoint, rightIntersectionPoint)));
                    plotLines.add(new PlotLine(List.of(topIntersectionPoint, leftIntersectionPoint)));
                } else {
                    plotLines.add(new PlotLine(List.of(bottomIntersectionPoint, leftIntersectionPoint)));
                    plotLines.add(new PlotLine(List.of(topIntersectionPoint, rightIntersectionPoint)));
                }
                return;
            }

            if (centerValueIsPositive) {
                plotLines.add(new PlotLine(List.of(bottomIntersectionPoint, leftIntersectionPoint)));
                plotLines.add(new PlotLine(List.of(rightIntersectionPoint, topIntersectionPoint)));
            } else {
                plotLines.add(new PlotLine(List.of(bottomIntersectionPoint, rightIntersectionPoint)));
                plotLines.add(new PlotLine(List.of(topIntersectionPoint, leftIntersectionPoint)));
            }
            return;
        }

        plotLines.add(new PlotLine(List.of(bottomIntersectionPoint, rightIntersectionPoint)));
        plotLines.add(new PlotLine(List.of(topIntersectionPoint, leftIntersectionPoint)));
    }

    private BigNumber tryEvaluateAt(final String expression, final Map<String, String> combinedVariables, final BigNumber xValue, final BigNumber yValue) {
        try {
            combinedVariables.put(ReservedVariables.X.getValue(), xValue.toString());
            combinedVariables.put(ReservedVariables.Y.getValue(), yValue.toString());
            return calculatorEngine.evaluate(expression, combinedVariables);
        } catch (final Exception ignored) {
            return null;
        }
    }

    private static int addUniquePlotPoint(final PlotPoint[] plotPoints, final int currentCount, final PlotPoint candidate) {
        if (candidate == null) {
            return currentCount;
        }

        for (int index = 0; index < currentCount; index++) {
            if (plotPoints[index].equals(candidate)) {
                return currentCount;
            }
        }

        plotPoints[currentCount] = candidate;
        return currentCount + 1;
    }

    private static int computeMarchingSquaresMask(final BigNumber bottomLeftCornerValue, final BigNumber bottomRightCornerValue, final BigNumber topRightCornerValue, final BigNumber topLeftCornerValue) {
        int mask = 0;

        if (bottomLeftCornerValue.isGreaterThan(ZERO)) {
            mask |= 1;
        }
        if (bottomRightCornerValue.isGreaterThan(ZERO)) {
            mask |= 2;
        }
        if (topRightCornerValue.isGreaterThan(ZERO)) {
            mask |= 4;
        }
        if (topLeftCornerValue.isGreaterThan(ZERO)) {
            mask |= 8;
        }

        return mask;
    }

    private PlotPoint computeIntersectionPointIfContourCrossesEdge(final BigNumber firstXValue, final BigNumber firstYValue, final BigNumber firstFunctionValue, final BigNumber secondXValue, final BigNumber secondYValue, final BigNumber secondFunctionValue) {
        if (firstFunctionValue.isEqualTo(ZERO) && secondFunctionValue.isEqualTo(ZERO)) {
            return null;
        }

        if (firstFunctionValue.isEqualTo(ZERO)) {
            return new PlotPoint(firstXValue, firstYValue);
        }
        if (secondFunctionValue.isEqualTo(ZERO)) {
            return new PlotPoint(secondXValue, secondYValue);
        }

        final int firstSign = firstFunctionValue.signum();
        final int secondSign = secondFunctionValue.signum();

        if (firstSign == 0 || secondSign == 0 || firstSign == secondSign) {
            return null;
        }

        final BigNumber denominator = firstFunctionValue.subtract(secondFunctionValue);
        if (denominator.isEqualTo(ZERO)) {
            return null;
        }

        final BigNumber interpolationFactor = firstFunctionValue.divide(denominator);

        final BigNumber interpolatedXValue = firstXValue.add(secondXValue.subtract(firstXValue).multiply(interpolationFactor));
        final BigNumber interpolatedYValue = firstYValue.add(secondYValue.subtract(firstYValue).multiply(interpolationFactor));

        return new PlotPoint(interpolatedXValue, interpolatedYValue);
    }

    private boolean isRequestValid(final String expression, final Map<String, String> variables, final ViewportSnapshot viewportSnapshot) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        if (viewportSnapshot == null) {
            return false;
        }
        if (viewportSnapshot.cellSize().isLessThanOrEqualTo(ZERO)) {
            return false;
        }
        if (variablesContainReservedVariables(variables.keySet())) {
            return false;
        }

        final BigNumber minimumXValue = viewportSnapshot.minX();
        final BigNumber maximumXValue = viewportSnapshot.maxX();
        final BigNumber minimumYValue = viewportSnapshot.minY();
        final BigNumber maximumYValue = viewportSnapshot.maxY();

        return minimumXValue.isLessThan(maximumXValue) && minimumYValue.isLessThan(maximumYValue);
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

}
