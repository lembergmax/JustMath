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
import com.mlprograms.justmath.graphfx.planar.model.*;
import com.mlprograms.justmath.graphfx.planar.view.ViewportSnapshot;
import lombok.NonNull;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static com.mlprograms.justmath.bignumber.BigNumbers.TWO;
import static com.mlprograms.justmath.bignumber.BigNumbers.ZERO;

public class GraphFxCalculatorEngine {

    private final CalculatorEngine CALCULATOR_ENGINE = new CalculatorEngine(new MathContext(10, RoundingMode.HALF_UP), TrigonometricMode.RAD);

    public PlotResult evaluate(@NonNull final String expression, @NonNull final ViewportSnapshot viewportSnapshot) {
        return evaluate(expression, Map.of(), viewportSnapshot);
    }

    public PlotResult evaluate(@NonNull final String expression, @NonNull final Map<String, String> variables, @NonNull final ViewportSnapshot viewportSnapshot) {
        if (!isRequestValid(expression, variables, viewportSnapshot)) {
            return new PlotResult();
        }

        final BigNumber minX = viewportSnapshot.minX();
        final BigNumber maxX = viewportSnapshot.maxX();
        final BigNumber minY = viewportSnapshot.minY();
        final BigNumber maxY = viewportSnapshot.maxY();
        final BigNumber step = viewportSnapshot.cellSize();

        final List<PlotLine> plotLines = new ArrayList<>();
        for (BigNumber y = minY; y.isLessThan(maxY); y = y.add(step)) {
            final BigNumber yAfterStep = y.add(step);
            if (yAfterStep.isGreaterThan(maxY)) {
                break;
            }

            for (BigNumber x = minX; x.isLessThan(maxX); x = x.add(step)) {
                final BigNumber xAfterStep = x.add(step);
                if (xAfterStep.isGreaterThan(maxX)) {
                    break;
                }

                final MarchingSquaresCell cell = evaluateMarchingSquaresCell(expression, variables, x, y, xAfterStep, yAfterStep);
                if (cell == null) {
                    continue;
                }

                plotLines.addAll(createSegmentsForCell(cell));
            }
        }

        return new PlotResult(new ArrayList<>(), plotLines);
    }

    private MarchingSquaresCell evaluateMarchingSquaresCell(final String expression, final Map<String, String> evaluationVariables, final BigNumber x1, final BigNumber y1, final BigNumber x2, final BigNumber y2) {
        final BigNumber bottomLeft = evaluate(expression, evaluationVariables, x1, y1);
        final BigNumber bottomRight = evaluate(expression, evaluationVariables, x2, y1);
        final BigNumber topRight = evaluate(expression, evaluationVariables, x2, y2);
        final BigNumber topLeft = evaluate(expression, evaluationVariables, x1, y2);

        if (bottomLeft == null || bottomRight == null || topRight == null || topLeft == null) {
            return null;
        }

        final BigNumber cx = x1.add(x2).divide(TWO);
        final BigNumber cy = y1.add(y2).divide(TWO);
        final BigNumber center = evaluate(expression, evaluationVariables, cx, cy);
        return new MarchingSquaresCell(x1, y1, x2, y2, bottomLeft, bottomRight, topRight, topLeft, center);
    }

    private List<PlotLine> createSegmentsForCell(final MarchingSquaresCell cell) {
        final PlotPoint pointBottom = intersectionIfCrosses(cell.x1(), cell.y1(), cell.valueBottomLeft(), cell.x2(), cell.y1(), cell.valueBottomRight());
        final PlotPoint pointRight = intersectionIfCrosses(cell.x2(), cell.y1(), cell.valueBottomRight(), cell.x2(), cell.y2(), cell.valueTopRight());
        final PlotPoint pointTop = intersectionIfCrosses(cell.x2(), cell.y2(), cell.valueTopRight(), cell.x1(), cell.y2(), cell.valueTopLeft());
        final PlotPoint pointLeft = intersectionIfCrosses(cell.x1(), cell.y2(), cell.valueTopLeft(), cell.x1(), cell.y1(), cell.valueBottomLeft());

        final List<PlotPoint> intersections = new ArrayList<>(4);
        final List<Edge> edges = new ArrayList<>(4);

        addIfNotNull(intersections, edges, pointBottom, Edge.BOTTOM);
        addIfNotNull(intersections, edges, pointRight, Edge.RIGHT);
        addIfNotNull(intersections, edges, pointTop, Edge.TOP);
        addIfNotNull(intersections, edges, pointLeft, Edge.LEFT);

        final int count = intersections.size();
        if (count == 0) {
            return List.of();
        }
        if (count == 2) {
            return List.of(new PlotLine(List.of(intersections.get(0), intersections.get(1))));
        }
        if (count == 4) {
            // Ambiger Fall (Case 5/10) -> Center entscheidet
            final boolean centerPositive = cell.centerValue() != null && cell.centerValue().isGreaterThan(ZERO);

            // Standard-Asymptotic-Decider-Style Pairing:
            // - Wenn Center positiv: “negative corners” werden getrennt, sonst “positive corners”
            // Wir brauchen dafür die tatsächliche Bit-Konstellation:
            final int mask = marchingMask(cell.valueBottomLeft(), cell.valueBottomRight(), cell.valueTopRight(), cell.valueTopLeft());
            return resolveAmbiguous(mask, centerPositive, pointBottom, pointRight, pointTop, pointLeft);
        }

        // count==1 oder 3 passiert bei exakten Nullen; kann man ignorieren oder extra behandeln
        return List.of();
    }

    private BigNumber evaluate(final String expression, final Map<String, String> variables, final BigNumber x, final BigNumber y) {
        try {
            final Map<String, String> combinedVariables = new HashMap<>(variables);
            combinedVariables.put(ReservedVariables.X.getValue(), x.toString());
            combinedVariables.put(ReservedVariables.Y.getValue(), y.toString());

            return CALCULATOR_ENGINE.evaluate(expression, combinedVariables);
        } catch (final Exception exception) {
            return null;
        }
    }

    private static int marchingMask(final BigNumber bl, final BigNumber br, final BigNumber tr, final BigNumber tl) {
        int mask = 0;
        if (bl.isGreaterThan(ZERO)) {
            mask |= 1; // bit0
        }
        if (br.isGreaterThan(ZERO)) {
            mask |= 2; // bit1
        }
        if (tr.isGreaterThan(ZERO)) {
            mask |= 4; // bit2
        }
        if (tl.isGreaterThan(ZERO)) {
            mask |= 8; // bit3
        }

        return mask;
    }

    private static List<PlotLine> resolveAmbiguous(final int mask, final boolean centerPositive, final PlotPoint bottom, final PlotPoint right, final PlotPoint top, final PlotPoint left) {
        // Ambige Masks: 5 (0101) und 10 (1010)
        if (mask == 5) {
            if (centerPositive) {
                return List.of(new PlotLine(List.of(bottom, right)), new PlotLine(List.of(top, left)));
            }
            return List.of(new PlotLine(List.of(bottom, left)), new PlotLine(List.of(top, right)));
        }

        if (mask == 10) {
            if (centerPositive) {
                return List.of(new PlotLine(List.of(bottom, left)), new PlotLine(List.of(right, top)));
            }
            return List.of(new PlotLine(List.of(bottom, right)), new PlotLine(List.of(top, left)));
        }

        // Falls du mal bei count==4 landest obwohl nicht 5/10:
        return List.of(new PlotLine(List.of(bottom, right)), new PlotLine(List.of(top, left)));
    }

    private PlotPoint intersectionIfCrosses(final BigNumber x1, final BigNumber y1, final BigNumber v1, final BigNumber x2, final BigNumber y2, final BigNumber v2) {
        if (!hasOppositeSigns(v1, v2)) {
            return null;
        }

        if (v1.isEqualTo(BigNumbers.ZERO)) {
            return new PlotPoint(x1, y1);
        }
        if (v2.isEqualTo(BigNumbers.ZERO)) {
            return new PlotPoint(x2, y2);
        }

        final BigNumber denominator = v1.subtract(v2);
        if (denominator.isEqualTo(ZERO)) {
            return null;
        }

        final BigNumber t = v1.divide(denominator);

        final BigNumber ix = x1.add(x2.subtract(x1).multiply(t));
        final BigNumber iy = y1.add(y2.subtract(y1).multiply(t));

        return new PlotPoint(ix, iy);
    }

    private static boolean hasOppositeSigns(final BigNumber a, final BigNumber b) {
        final boolean aPos = a.isGreaterThan(ZERO);
        final boolean bPos = b.isGreaterThan(ZERO);
        final boolean aNeg = a.isLessThan(ZERO);
        final boolean bNeg = b.isLessThan(ZERO);

        return (aPos && bNeg) || (aNeg && bPos);
    }

    private static void addIfNotNull(final List<PlotPoint> points, final List<Edge> edges, final PlotPoint point, final Edge edge) {
        if (point == null) {
            return;
        }

        points.add(point);
        edges.add(edge);
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

        if (viewportSnapshot.cellSize().isLessThanOrEqualTo(ZERO)) {
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
