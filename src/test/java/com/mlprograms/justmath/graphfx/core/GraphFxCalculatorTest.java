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

package com.mlprograms.justmath.graphfx.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class GraphFxCalculatorTest {

    private static final GraphFxCalculator.PlotCancellation NO_CANCEL = () -> false;

    @Test
    void containsYVariableDetectsStandaloneYOnly() {
        final GraphFxCalculator calc = new GraphFxCalculator();

        assertTrue(calc.containsYVariable("x+y"));
        assertTrue(calc.containsYVariable("Y+1"));
        assertTrue(calc.containsYVariable("sin(y)"));

        assertFalse(calc.containsYVariable("myVar + 1"));
        assertFalse(calc.containsYVariable("y1 + 2"));
        assertFalse(calc.containsYVariable("xy + 2"));
        assertFalse(calc.containsYVariable("abc_def + 2"));
    }

    @Test
    void plotExplicitGeneratesPolyline() {
        final GraphFxCalculator calc = new GraphFxCalculator();
        final var bounds = new GraphFxCalculator.WorldBounds(-2, 2, -5, 5);

        final GraphFxCalculator.PlotGeometry g = calc.plot(
                "x^2",
                Map.of(),
                bounds,
                800,
                600,
                NO_CANCEL
        );

        assertFalse(g.polyline().isEmpty());
        assertTrue(g.segments().isEmpty());

        // Prüfe: Punkte erfüllen y≈x^2 (ignoriere NaN Trenner)
        for (GraphFxPoint p : g.polyline()) {
            if (!Double.isFinite(p.x()) || !Double.isFinite(p.y())) continue;
            assertEquals(p.x() * p.x(), p.y(), 1e-6);
        }
    }

    @Test
    void plotImplicitLinearFastPathCreatesSingleSegment() {
        final GraphFxCalculator calc = new GraphFxCalculator();
        final var bounds = new GraphFxCalculator.WorldBounds(-10, 10, -10, 10);

        final GraphFxCalculator.PlotGeometry g = calc.plot(
                "2x-3y+4,3",
                Map.of(),
                bounds,
                1000,
                700,
                NO_CANCEL
        );

        assertTrue(g.polyline().isEmpty());
        assertEquals(1, g.segments().size());

        final var s = g.segments().getFirst();

        assertTrue(isInside(bounds, s.a()));
        assertTrue(isInside(bounds, s.b()));

        // typisch für "clipped line": mindestens ein Endpunkt liegt auf dem Rand
        assertTrue(isOnBorder(bounds, s.a(), 1e-9) || isOnBorder(bounds, s.b(), 1e-9));
    }

    private static boolean isInside(GraphFxCalculator.WorldBounds b, GraphFxPoint p) {
        final double minX = Math.min(b.minX(), b.maxX());
        final double maxX = Math.max(b.minX(), b.maxX());
        final double minY = Math.min(b.minY(), b.maxY());
        final double maxY = Math.max(b.minY(), b.maxY());
        return p.x() >= minX && p.x() <= maxX && p.y() >= minY && p.y() <= maxY;
    }

    private static boolean isOnBorder(GraphFxCalculator.WorldBounds b, GraphFxPoint p, double eps) {
        final double minX = Math.min(b.minX(), b.maxX());
        final double maxX = Math.max(b.minX(), b.maxX());
        final double minY = Math.min(b.minY(), b.maxY());
        final double maxY = Math.max(b.minY(), b.maxY());
        return Math.abs(p.x() - minX) <= eps
                || Math.abs(p.x() - maxX) <= eps
                || Math.abs(p.y() - minY) <= eps
                || Math.abs(p.y() - maxY) <= eps;
    }

    @Test
    void marchingSquaresCircleMidpointsNearZero() {
        final GraphFxCalculator calc = new GraphFxCalculator();
        final var bounds = new GraphFxCalculator.WorldBounds(-2, 2, -2, 2);

        final List<GraphFxCalculator.LineSegment> segs = calc.createImplicitZeroContourSegments(
                "x^2 + y^2 - 1",
                Map.of(),
                bounds,
                80,
                80,
                NO_CANCEL
        );

        assertFalse(segs.isEmpty());

        // marching squares -> gröbere Toleranz
        final double tol = 2e-2;
        for (var s : segs) {
            final double mx = (s.a().x() + s.b().x()) * 0.5;
            final double my = (s.a().y() + s.b().y()) * 0.5;
            final double f = mx * mx + my * my - 1.0;
            assertTrue(Math.abs(f) <= tol);
        }
    }

    @Test
    void cancellationReturnsEmpty() {
        final GraphFxCalculator calc = new GraphFxCalculator();
        final var bounds = new GraphFxCalculator.WorldBounds(-50, 50, -50, 50);

        final GraphFxCalculator.PlotCancellation cancelImmediately = () -> true;

        final GraphFxCalculator.PlotGeometry g = calc.plot(
                "x^2",
                Map.of(),
                bounds,
                1200,
                800,
                cancelImmediately
        );

        assertTrue(g.polyline().isEmpty());
        assertTrue(g.segments().isEmpty());
    }

}
